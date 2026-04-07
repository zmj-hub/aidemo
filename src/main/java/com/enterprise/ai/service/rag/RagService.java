package com.enterprise.ai.service.rag;

import cn.hutool.core.util.StrUtil;
import com.enterprise.ai.common.exception.BusinessException;
import com.enterprise.ai.common.utils.UserContextHolder;
import com.enterprise.ai.config.RagConfig;
import com.enterprise.ai.config.RagProperties;
import com.enterprise.ai.domain.dto.DocumentInfo;
import com.enterprise.ai.domain.dto.RagQueryRequest;
import com.enterprise.ai.domain.dto.RagQueryResponse;
import com.enterprise.ai.domain.entity.Document;
import com.enterprise.ai.service.model.ChatModelService;
import com.enterprise.ai.service.model.ModelFactory;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * RAG服务类
 * 支持动态选择对话模型和Embedding模型，提供RAG问答，支持答案溯源
 */
@Slf4j
@Service
public class RagService {

    @Autowired
    private RagProperties ragProperties;

    @Autowired
    private RagConfig ragConfig;

    @Autowired
    private EmbeddingStore<TextSegment> embeddingStore;

    @Autowired
    private ChatModelService chatModelService;

    @Autowired
    private ModelFactory modelFactory;

    /**
     * 文档元数据缓存，用于快速查找文档名称
     */
    @Autowired
    private DocumentService documentService;

    /**
     * RAG查询
     * 
     * @param request 查询请求
     * @return 查询响应
     */
    public RagQueryResponse query(RagQueryRequest request) {
        Long userId = UserContextHolder.getUserId();
        if (userId == null) {
            throw new BusinessException("请先登录");
        }

        String chatModelName = StrUtil.isBlank(request.getChatModel()) 
                ? ragProperties.getDefaultChatModel() 
                : request.getChatModel();
        String embeddingModelName = StrUtil.isBlank(request.getEmbeddingModel()) 
                ? ragProperties.getDefaultEmbeddingModel() 
                : request.getEmbeddingModel();
        Integer maxResults = request.getMaxResults() != null 
                ? request.getMaxResults() 
                : ragProperties.getMaxResults();
        Double minScore = request.getMinScore() != null 
                ? request.getMinScore() 
                : ragProperties.getMinScore();

        List<EmbeddingMatch<TextSegment>> relevantSegments = retrieveRelevantSegments(
                request.getQuery(), embeddingModelName, userId, maxResults, minScore);

        String context = buildContext(relevantSegments);

        String answer = generateAnswer(request.getQuery(), context, chatModelName);

        List<RagQueryResponse.SourceDocument> sources = buildSourceDocuments(relevantSegments);

        return RagQueryResponse.builder()
                .answer(answer)
                .chatModel(chatModelName)
                .embeddingModel(embeddingModelName)
                .sources(sources)
                .build();
    }

    /**
     * 检索相关的文本片段
     * 
     * @param query 查询问题
     * @param embeddingModelName Embedding模型名称
     * @param userId 用户ID
     * @param maxResults 最大返回结果数
     * @param minScore 最小相似度阈值
     * @return 相关的文本片段列表
     */
    private List<EmbeddingMatch<TextSegment>> retrieveRelevantSegments(String query, 
            String embeddingModelName, Long userId, Integer maxResults, Double minScore) {
        EmbeddingModel embeddingModel = ragConfig.getEmbeddingModel(embeddingModelName);

        List<EmbeddingMatch<TextSegment>> allMatches = new ArrayList<>();
        try {
            if (embeddingModel != null) {
                allMatches = embeddingStore.findRelevant(
                        embeddingModel.embed(query).content(),
                        maxResults * 2,
                        0.0
                );
            }
        } catch (Exception e) {
            log.warn("Embedding模型调用失败", e);
        }

        return allMatches.stream()
                .filter(match -> {
                    String segmentUserId = match.embedded().metadata().getString("user_id");
                    return segmentUserId != null && segmentUserId.equals(userId.toString());
                })
                .filter(match -> match.score() >= minScore)
                .limit(maxResults)
                .collect(Collectors.toList());
    }

    /**
     * 构建上下文
     * 
     * @param segments 相关文本片段
     * @return 上下文字符串
     */
    private String buildContext(List<EmbeddingMatch<TextSegment>> segments) {
        if (segments.isEmpty()) {
            return "没有找到相关的文档内容。";
        }

        StringBuilder context = new StringBuilder();
        context.append("以下是相关的文档内容：\n\n");

        int index = 1;
        for (EmbeddingMatch<TextSegment> match : segments) {
            TextSegment segment = match.embedded();
            String documentId = segment.metadata().getString("document_id");
            
            context.append("[文档").append(index).append("] ");
            if (documentId != null) {
                context.append("(文档ID: ").append(documentId).append(") ");
            }
            context.append("相似度: ").append(String.format("%.2f", match.score())).append("\n");
            context.append(segment.text()).append("\n\n");
            index++;
        }

        return context.toString();
    }

    /**
     * 生成答案
     * 
     * @param query 用户查询
     * @param context 上下文
     * @param chatModelName Chat模型名称
     * @return 生成的答案
 */
    private String generateAnswer(String query, String context, String chatModelName) {
        String systemPrompt = "你是一个专业的问答助手。请根据提供的文档内容回答用户的问题。\n" +
                "回答要求：\n" +
                "1. 只基于提供的文档内容回答，不要使用文档中没有的信息\n" +
                "2. 如果文档中没有相关信息，请明确告知用户\n" +
                "3. 回答要简洁明了，重点突出\n" +
                "4. 可以适当引用文档中的内容来支持你的回答\n\n" +
                "文档内容：\n" + context;

        ChatLanguageModel chatModel = getChatModel(chatModelName);
        
        Response<AiMessage> response = chatModel.chat(
                SystemMessage.from(systemPrompt),
                UserMessage.from(query)
        );

        return response.content().text();
    }

    /**
     * 获取Chat模型
     * 
     * @param modelName 模型名称
     * @return Chat模型
     */
    private ChatLanguageModel getChatModel(String modelName) {
        ChatLanguageModel model = modelFactory.getChatModel(modelName);
        if (model == null) {
            throw new BusinessException("不支持的Chat模型：" + modelName);
        }
        return model;
    }

    /**
     * 构建溯源文档列表
     * 
     * @param segments 相关文本片段
     * @return 溯源文档列表
     */
    private List<RagQueryResponse.SourceDocument> buildSourceDocuments(
            List<EmbeddingMatch<TextSegment>> segments) {
        if (segments.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, Document> documentCache = new ConcurrentHashMap<>();
        
        return segments.stream()
                .map(match -> {
                    TextSegment segment = match.embedded();
                    String documentId = segment.metadata().getString("document_id");
                    
                    String documentName = "未知文档";
                    if (documentId != null) {
                        try {
                            DocumentInfo docInfo = documentService.getDocumentById(documentId);
                            documentName = docInfo.getDocumentName();
                        } catch (Exception e) {
                        }
                    }

                    return RagQueryResponse.SourceDocument.builder()
                            .documentId(documentId)
                            .documentName(documentName)
                            .content(segment.text())
                            .score(match.score())
                            .build();
                })
                .collect(Collectors.toList());
    }
}
