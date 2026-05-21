package com.enterprise.ai.controller;

import com.enterprise.ai.common.result.Result;
import com.enterprise.ai.domain.dto.DocumentInfo;
import com.enterprise.ai.domain.dto.DocumentUploadRequest;
import com.enterprise.ai.domain.dto.RagQueryRequest;
import com.enterprise.ai.domain.dto.RagQueryResponse;
import com.enterprise.ai.service.rag.DocumentService;
import com.enterprise.ai.service.rag.RagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "RAG管理模块", description = "RAG文档问答相关接口")
@RestController
@RequestMapping("/rag")
@RequiredArgsConstructor
public class RagController {

    private final RagService ragService;
    private final DocumentService documentService;

    @Operation(summary = "上传文档")
    @PostMapping("/documents")
    public Result<DocumentInfo> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "documentName", required = false) String documentName,
            @RequestParam(value = "description", required = false) String description) {
        return Result.success(documentService.uploadDocument(file, documentName, description));
    }

    @Operation(summary = "删除文档")
    @DeleteMapping("/documents/{id}")
    public Result<Void> deleteDocument(@PathVariable String id) {
        documentService.deleteDocument(id);
        return Result.success();
    }

    @Operation(summary = "获取文档列表")
    @GetMapping("/documents")
    public Result<List<DocumentInfo>> listDocuments() {
        return Result.success(documentService.getUserDocuments());
    }

    @Operation(summary = "获取文档详情")
    @GetMapping("/documents/{id}")
    public Result<DocumentInfo> getDocument(@PathVariable String id) {
        DocumentInfo doc = documentService.getDocumentById(id);
        if (doc == null) {
            return Result.error("文档不存在");
        }
        return Result.success(doc);
    }

    @Operation(summary = "RAG问答")
    @PostMapping("/query")
    public Result<RagQueryResponse> query(@RequestBody RagQueryRequest request) {
        return Result.success(ragService.query(
                request.getQuery(),
                request.getChatModel(),
                request.getMaxResults() != null ? request.getMaxResults() : 5,
                request.getMinScore() != null ? request.getMinScore() : 0.7
        ));
    }
}
