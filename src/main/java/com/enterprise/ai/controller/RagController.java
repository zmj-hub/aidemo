package com.enterprise.ai.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.enterprise.ai.common.result.Result;
import com.enterprise.ai.domain.dto.DocumentInfo;
import com.enterprise.ai.domain.dto.DocumentUploadRequest;
import com.enterprise.ai.domain.dto.RagQueryRequest;
import com.enterprise.ai.domain.dto.RagQueryResponse;
import com.enterprise.ai.service.rag.DocumentService;
import com.enterprise.ai.service.rag.RagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * RAG控制器
 * 提供文档上传、删除、列表查询、RAG问答接口
 */
@Tag(name = "RAG模块", description = "RAG文档管理和问答相关接口")
@RestController
@RequestMapping("/rag")
public class RagController {

    @Autowired
    private DocumentService documentService;

    @Autowired
    private RagService ragService;

    /**
     * 上传文档接口
     * 
     * @param file 上传的文件
     * @param request 文档上传请求参数
     * @return 文档信息
     */
    @Operation(summary = "上传文档", description = "上传文档并进行解析、分块、向量化存储")
    @SaCheckLogin
    @PostMapping(value = "/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<DocumentInfo> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @Valid DocumentUploadRequest request) {
        DocumentInfo documentInfo = documentService.uploadDocument(file, request);
        return Result.success("文档上传成功", documentInfo);
    }

    /**
     * 删除文档接口
     * 
     * @param documentId 文档ID
     * @return 删除结果
     */
    @Operation(summary = "删除文档", description = "删除指定文档及其向量数据")
    @SaCheckLogin
    @DeleteMapping("/documents/{documentId}")
    public Result<String> deleteDocument(@PathVariable String documentId) {
        documentService.deleteDocument(documentId);
        return Result.success("文档删除成功");
    }

    /**
     * 获取文档列表接口
     * 
     * @return 文档列表
     */
    @Operation(summary = "获取文档列表", description = "获取当前用户的所有文档列表")
    @SaCheckLogin
    @GetMapping("/documents")
    public Result<List<DocumentInfo>> getDocuments() {
        List<DocumentInfo> documents = documentService.getUserDocuments();
        return Result.success(documents);
    }

    /**
     * 获取文档详情接口
     * 
     * @param documentId 文档ID
     * @return 文档详情
     */
    @Operation(summary = "获取文档详情", description = "获取指定文档的详细信息")
    @SaCheckLogin
    @GetMapping("/documents/{documentId}")
    public Result<DocumentInfo> getDocument(@PathVariable String documentId) {
        DocumentInfo documentInfo = documentService.getDocumentById(documentId);
        return Result.success(documentInfo);
    }

    /**
     * RAG问答接口
     * 
     * @param request RAG查询请求参数
     * @return RAG查询响应
     */
    @Operation(summary = "RAG问答", description = "基于上传的文档进行智能问答，支持答案溯源")
    @SaCheckLogin
    @PostMapping("/query")
    public Result<RagQueryResponse> query(@Valid @RequestBody RagQueryRequest request) {
        RagQueryResponse response = ragService.query(request);
        return Result.success(response);
    }
}
