package com.enterprise.ai.service.rag;

import cn.dev33.satoken.stp.StpUtil;
import com.enterprise.ai.config.PlatformProperties;
import com.enterprise.ai.domain.dto.DocumentInfo;
import com.enterprise.ai.domain.entity.Document;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentService {

    private final PlatformProperties platformProperties;
    private final Map<String, Document> documentStore = new ConcurrentHashMap<>();

    public DocumentInfo uploadDocument(MultipartFile file, String documentName, String description) {
        String documentId = UUID.randomUUID().toString();
        try {
            Path storagePath = Path.of(platformProperties.getRag().getDocumentStoragePath());
            Files.createDirectories(storagePath);
            Path targetPath = storagePath.resolve(documentId + "_" + file.getOriginalFilename());
            file.transferTo(targetPath.toFile());

            Document doc = new Document();
            doc.setDocumentId(documentId);
            doc.setUserId(getCurrentUserId());
            doc.setDocumentName(documentName != null ? documentName : file.getOriginalFilename());
            doc.setDescription(description);
            doc.setFileType(getFileType(file.getOriginalFilename()));
            doc.setFileSize(file.getSize());
            doc.setFilePath(targetPath.toString());
            doc.setChunkCount(0);
            doc.setUploadTime(LocalDateTime.now());
            doc.setUpdateTime(LocalDateTime.now());

            documentStore.put(documentId, doc);
            log.info("Document uploaded: {} ({})", documentId, doc.getDocumentName());

            return toDocumentInfo(doc);
        } catch (IOException e) {
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }

    public void deleteDocument(String documentId) {
        Document doc = documentStore.get(documentId);
        if (doc == null) return;

        try {
            Files.deleteIfExists(Path.of(doc.getFilePath()));
        } catch (IOException ignored) {}
        documentStore.remove(documentId);
    }

    public List<DocumentInfo> getUserDocuments() {
        return documentStore.values().stream()
                .filter(d -> d.getUserId().equals(getCurrentUserId()))
                .map(this::toDocumentInfo)
                .sorted(Comparator.comparing(DocumentInfo::getUploadTime).reversed())
                .toList();
    }

    public DocumentInfo getDocumentById(String documentId) {
        Document doc = documentStore.get(documentId);
        if (doc == null) return null;
        return toDocumentInfo(doc);
    }

    private DocumentInfo toDocumentInfo(Document doc) {
        return DocumentInfo.builder()
                .documentId(doc.getDocumentId())
                .documentName(doc.getDocumentName())
                .description(doc.getDescription())
                .fileType(doc.getFileType())
                .fileSize(doc.getFileSize())
                .chunkCount(doc.getChunkCount())
                .uploadTime(doc.getUploadTime())
                .build();
    }

    private String getFileType(String filename) {
        if (filename == null) return "UNKNOWN";
        String ext = filename.substring(filename.lastIndexOf('.') + 1).toUpperCase();
        return switch (ext) {
            case "PDF" -> "PDF";
            case "DOC", "DOCX" -> "WORD";
            case "MD" -> "MARKDOWN";
            case "TXT" -> "TEXT";
            default -> ext;
        };
    }

    private Long getCurrentUserId() {
        try {
            return StpUtil.getLoginIdAsLong();
        } catch (Exception e) {
            return 1L;
        }
    }
}
