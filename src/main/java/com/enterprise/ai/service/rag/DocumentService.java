package com.enterprise.ai.service.rag;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.enterprise.ai.common.exception.BusinessException;
import com.enterprise.ai.config.RagConfig;
import com.enterprise.ai.config.RagProperties;
import com.enterprise.ai.domain.dto.DocumentInfo;
import com.enterprise.ai.domain.dto.DocumentUploadRequest;
import com.enterprise.ai.domain.entity.Document;
import com.enterprise.ai.common.utils.UserContextHolder;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser;
import dev.langchain4j.data.document.parser.apache.poi.ApachePoiDocumentParser;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 文档服务类
 * 负责文档上传、解析、分块、向量化存储等功能
 */
@Service
public class DocumentService {

    @Autowired
    private RagProperties ragProperties;

    @Autowired
    private RagConfig ragConfig;

    @Autowired
    private EmbeddingStore<TextSegment> embeddingStore;

    /**
     * 内存中存储文档元数据（实际项目中应使用数据库）
     */
    private final Map<String, Document> documentStore = new ConcurrentHashMap<>();

    /**
     * 用户文档索引：userId -> List<documentId>
     */
    private final Map<Long, Set<String>> userDocumentIndex = new ConcurrentHashMap<>();

    /**
     * 上传并处理文档
     * 
     * @param file 上传的文件
     * @param request 文档上传请求
     * @return 文档信息
     */
    public DocumentInfo uploadDocument(MultipartFile file, DocumentUploadRequest request) {
        Long userId = UserContextHolder.getUserId();
        String documentId = IdUtil.fastSimpleUUID();
        
        try {
            String fileType = getFileType(file.getOriginalFilename());
            String storagePath = saveFile(file, documentId, fileType);
            
            List<TextSegment> segments = parseAndSplitDocument(file.getInputStream(), fileType);
            
            String embeddingModel = request.getEmbeddingModel();
            if (StrUtil.isBlank(embeddingModel)) {
                embeddingModel = ragProperties.getDefaultEmbeddingModel();
            }
            
            List<String> embeddingIds = storeEmbeddings(segments, embeddingModel, documentId, userId);
            
            Document document = buildDocument(documentId, userId, request, file, fileType, 
                    storagePath, segments.size(), embeddingModel, embeddingIds);
            
            saveDocumentMetadata(document);
            
            return convertToDocumentInfo(document);
            
        } catch (IOException e) {
            throw new BusinessException("文档处理失败：" + e.getMessage());
        }
    }

    /**
     * 删除文档
     * 
     * @param documentId 文档ID
     */
    public void deleteDocument(String documentId) {
        Long userId = UserContextHolder.getUserId();
        Document document = documentStore.get(documentId);
        
        if (document == null) {
            throw new BusinessException("文档不存在");
        }
        
        if (!document.getUserId().equals(userId)) {
            throw new BusinessException("无权删除该文档");
        }
        
        deleteEmbeddings(document.getEmbeddingIds());
        deleteFile(document.getFilePath());
        removeDocumentMetadata(documentId, userId);
    }

    /**
     * 获取当前用户的文档列表
     * 
     * @return 文档信息列表
     */
    public List<DocumentInfo> getUserDocuments() {
        Long userId = UserContextHolder.getUserId();
        Set<String> documentIds = userDocumentIndex.getOrDefault(userId, Collections.emptySet());
        
        return documentIds.stream()
                .map(documentStore::get)
                .filter(Objects::nonNull)
                .map(this::convertToDocumentInfo)
                .sorted(Comparator.comparing(DocumentInfo::getUploadTime).reversed())
                .collect(Collectors.toList());
    }

    /**
     * 根据文档ID获取文档信息
     * 
     * @param documentId 文档ID
     * @return 文档信息
     */
    public DocumentInfo getDocumentById(String documentId) {
        Long userId = UserContextHolder.getUserId();
        Document document = documentStore.get(documentId);
        
        if (document == null) {
            throw new BusinessException("文档不存在");
        }
        
        if (!document.getUserId().equals(userId)) {
            throw new BusinessException("无权访问该文档");
        }
        
        return convertToDocumentInfo(document);
    }

    /**
     * 获取文件类型
     * 
     * @param filename 文件名
     * @return 文件类型
     */
    private String getFileType(String filename) {
        if (StrUtil.isBlank(filename)) {
            throw new BusinessException("文件名不能为空");
        }
        
        String ext = FileUtil.extName(filename).toLowerCase();
        return switch (ext) {
            case "pdf" -> "PDF";
            case "doc", "docx" -> "WORD";
            case "md", "markdown" -> "MARKDOWN";
            case "txt" -> "TEXT";
            default -> throw new BusinessException("不支持的文件类型：" + ext);
        };
    }

    /**
     * 保存文件到本地
     * 
     * @param file 文件
     * @param documentId 文档ID
     * @param fileType 文件类型
     * @return 文件存储路径
     */
    private String saveFile(MultipartFile file, String documentId, String fileType) throws IOException {
        String storagePath = ragProperties.getDocumentStoragePath();
        File storageDir = new File(storagePath);
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }
        
        String ext = fileType.toLowerCase();
        String filePath = storagePath + File.separator + documentId + "." + ext;
        file.transferTo(new File(filePath));
        
        return filePath;
    }

    /**
     * 解析并分块文档
     * 
     * @param inputStream 文件输入流
     * @param fileType 文件类型
     * @return 文本片段列表
     */
    private List<TextSegment> parseAndSplitDocument(InputStream inputStream, String fileType) {
        DocumentParser parser = switch (fileType) {
            case "PDF" -> new ApachePdfBoxDocumentParser();
            case "WORD" -> new ApachePoiDocumentParser();
            case "MARKDOWN", "TEXT" -> new TextDocumentParser();
            default -> throw new BusinessException("不支持的文件类型");
        };
        
        dev.langchain4j.data.document.Document document = parser.parse(inputStream);
        
        return EmbeddingStoreIngestor.builder()
                .documentSplitter(new dev.langchain4j.data.document.splitter.DocumentByParagraphSplitter(
                        ragProperties.getChunkSize(), ragProperties.getChunkOverlap()))
                .build()
                .split(document);
    }

    /**
     * 存储向量
     * 
     * @param segments 文本片段
     * @param embeddingModelName Embedding模型名称
     * @param documentId 文档ID
     * @param userId 用户ID
     * @return 向量ID列表
     */
    private List<String> storeEmbeddings(List<TextSegment> segments, String embeddingModelName, 
                                         String documentId, Long userId) {
        EmbeddingModel embeddingModel = ragConfig.getEmbeddingModel(embeddingModelName);
        
        List<TextSegment> enhancedSegments = segments.stream()
                .map(segment -> {
                    Map<String, Object> metadata = new HashMap<>(segment.metadata().toMap());
                    metadata.put("document_id", documentId);
                    metadata.put("user_id", userId.toString());
                    return TextSegment.from(segment.text(), dev.langchain4j.data.document.Metadata.from(metadata));
                })
                .collect(Collectors.toList());
        
        return embeddingStore.addAll(enhancedSegments, embeddingModel);
    }

    /**
     * 构建文档实体
     */
    private Document buildDocument(String documentId, Long userId, DocumentUploadRequest request, 
                                   MultipartFile file, String fileType, String filePath, 
                                   int chunkCount, String embeddingModel, List<String> embeddingIds) {
        Document document = new Document();
        document.setDocumentId(documentId);
        document.setUserId(userId);
        document.setDocumentName(request.getDocumentName());
        document.setDescription(request.getDescription());
        document.setFileType(fileType);
        document.setFileSize(file.getSize());
        document.setFilePath(filePath);
        document.setChunkCount(chunkCount);
        document.setEmbeddingModel(embeddingModel);
        document.setEmbeddingIds(String.join(",", embeddingIds));
        document.setUploadTime(LocalDateTime.now());
        document.setUpdateTime(LocalDateTime.now());
        return document;
    }

    /**
     * 保存文档元数据
     */
    private void saveDocumentMetadata(Document document) {
        documentStore.put(document.getDocumentId(), document);
        userDocumentIndex.computeIfAbsent(document.getUserId(), k -> new HashSet<>())
                .add(document.getDocumentId());
    }

    /**
     * 删除向量
     */
    private void deleteEmbeddings(String embeddingIdsStr) {
        if (StrUtil.isNotBlank(embeddingIdsStr)) {
            String[] ids = embeddingIdsStr.split(",");
            for (String id : ids) {
                try {
                    embeddingStore.remove(id);
                } catch (Exception e) {
                }
            }
        }
    }

    /**
     * 删除本地文件
     */
    private void deleteFile(String filePath) {
        if (StrUtil.isNotBlank(filePath)) {
            FileUtil.del(filePath);
        }
    }

    /**
     * 移除文档元数据
     */
    private void removeDocumentMetadata(String documentId, Long userId) {
        documentStore.remove(documentId);
        Set<String> userDocs = userDocumentIndex.get(userId);
        if (userDocs != null) {
            userDocs.remove(documentId);
        }
    }

    /**
     * 转换为DocumentInfo
     */
    private DocumentInfo convertToDocumentInfo(Document document) {
        return DocumentInfo.builder()
                .documentId(document.getDocumentId())
                .documentName(document.getDocumentName())
                .description(document.getDescription())
                .fileType(document.getFileType())
                .fileSize(document.getFileSize())
                .chunkCount(document.getChunkCount())
                .embeddingModel(document.getEmbeddingModel())
                .uploadTime(document.getUploadTime())
                .build();
    }
}
