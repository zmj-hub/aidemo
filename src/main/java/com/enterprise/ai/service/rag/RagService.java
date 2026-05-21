package com.enterprise.ai.service.rag;

import com.enterprise.ai.config.PlatformProperties;
import com.enterprise.ai.domain.dto.RagQueryResponse;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
import io.agentscope.core.model.Model;
import io.agentscope.core.model.ModelRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RagService {

    private final PlatformProperties platformProperties;
    private final DocumentService documentService;

    public RagQueryResponse query(String queryText, String chatModelCode, int maxResults, double minScore) {
        String modelId = resolveModelId(chatModelCode);
        RagQueryResponse response = RagQueryResponse.builder()
                .chatModel(modelId)
                .embeddingModel("none") // 暂未配置Embedding模型
                .sources(new ArrayList<>())
                .build();

        try {
            // 简化版RAG：基于文档内容的关键词检索
            StringBuilder context = new StringBuilder();
            List<RagQueryResponse.SourceDocument> sources = new ArrayList<>();

            var docs = documentService.getUserDocuments();
            for (var docInfo : docs) {
                if (sources.size() >= maxResults) break;
                // 简单的关键词匹配
                String docName = docInfo.getDocumentName() != null ? docInfo.getDocumentName().toLowerCase() : "";
                String queryLower = queryText.toLowerCase();
                if (docName.contains(queryLower) || queryLower.contains(docName)
                        || (docInfo.getDescription() != null && docInfo.getDescription().toLowerCase().contains(queryLower))) {
                    sources.add(RagQueryResponse.SourceDocument.builder()
                            .documentId(docInfo.getDocumentId())
                            .documentName(docInfo.getDocumentName())
                            .content(docInfo.getDescription() != null ? docInfo.getDescription() : docName)
                            .score(1.0)
                            .build());
                    if (docInfo.getDescription() != null) {
                        context.append(docInfo.getDescription()).append("\n");
                    }
                }
            }
            response.setSources(sources);

            if (!sources.isEmpty() && ModelRegistry.canResolve(modelId)) {
                Model model = ModelRegistry.resolve(modelId);
                String prompt = String.format(
                        "基于以下文档内容回答问题。仅使用提供的文档内容，如果文档中没有相关信息请明确说明。\n\n文档内容:\n%s\n问题: %s",
                        context.toString(), queryText);

                Msg userMsg = Msg.builder()
                        .role(MsgRole.USER)
                        .textContent(prompt)
                        .build();
                String answerText = model.stream(List.of(userMsg), List.of(), null)
                        .collectList()
                        .block()
                        .stream()
                        .map(r -> {
                            if (!r.getContent().isEmpty() && r.getContent().get(0) instanceof io.agentscope.core.message.TextBlock tb) {
                                return tb.getText();
                            }
                            return "";
                        })
                        .reduce("", (a, b) -> a + b);
                response.setAnswer(!answerText.isEmpty() ? answerText : "无法生成回答");
            } else {
                response.setAnswer("未找到相关文档或模型不可用。请先上传相关文档。");
            }

        } catch (Exception e) {
            log.error("RAG query failed", e);
            response.setAnswer("RAG查询失败: " + e.getMessage());
        }

        return response;
    }

    private String resolveModelId(String modelCode) {
        if (modelCode == null) {
            modelCode = platformProperties.getRag().getDefaultChatModel();
        }
        if (modelCode.contains(":")) return modelCode;
        return "modelscope:" + modelCode;
    }
}
