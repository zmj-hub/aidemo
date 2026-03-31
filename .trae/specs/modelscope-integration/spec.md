# ModelScope模型集成 - 产品需求文档

## Overview
- **Summary**: 移除项目中所有OpenAI相关的模型调用配置，添加ModelScope模型的完整接入支持，实现与ModelScope服务的无缝对接。
- **Purpose**: 将现有的基于OpenAI兼容接口的模型配置替换为原生ModelScope支持，消除对OpenAI服务的依赖。
- **Target Users**: 项目开发者、AI应用集成人员

## Goals
- 移除所有OpenAI相关的依赖和配置
- 添加ModelScope API密钥配置
- 实现ModelScope模型选择参数
- 定义ModelScope请求格式
- 设置ModelScope认证机制
- 实现ModelScope响应处理逻辑
- 确保ModelScope服务能正确连接和调用

## Non-Goals (Out of Scope)
- 不修改除模型配置外的其他业务逻辑
- 不修改RAG模块的Embedding配置（保持all-minilm-l6-v2）
- 不修改现有的鉴权体系
- 不修改Docker配置

## Background & Context
- 当前项目使用langchain4j-open-ai依赖来接入千问和DeepSeek模型（通过OpenAI兼容接口）
- 需要替换为ModelScope原生支持
- ModelScope提供了丰富的开源模型资源

## Functional Requirements
- **FR-1**: 移除所有langchain4j-open-ai依赖
- **FR-2**: 添加ModelScope相关依赖
- **FR-3**: 创建ModelScope配置属性类
- **FR-4**: 修改ModelConfig配置类，移除OpenAI相关Bean，添加ModelScope模型Bean
- **FR-5**: 修改ModelProvider枚举，添加ModelScope模型
- **FR-6**: 修改application.yml配置，移除OpenAI相关配置，添加ModelScope配置
- **FR-7**: 修改ModelFactory和ChatModelService，适配ModelScope模型
- **FR-8**: 更新README文档中关于ModelScope的说明

## Non-Functional Requirements
- **NFR-1**: 保持现有API接口不变
- **NFR-2**: 确保代码编译通过，无错误
- **NFR-3**: 保持完整的中文注释
- **NFR-4**: 符合阿里巴巴Java开发规范

## Constraints
- **Technical**: 继续使用LangChain4j 0.34.0、Spring Boot 3.3.5、JDK 21
- **Dependencies**: 需要使用ModelScope相关的LangChain4j集成

## Assumptions
- ModelScope提供OpenAI兼容的API接口
- 可以通过langchain4j-open-ai库兼容ModelScope的API
- 用户已具备ModelScope的API密钥

## Acceptance Criteria

### AC-1: OpenAI依赖已移除
- **Given**: 项目已修改
- **When**: 检查pom.xml
- **Then**: langchain4j-open-ai依赖已被移除
- **Verification**: `programmatic`

### AC-2: ModelScope配置已添加
- **Given**: 项目已修改
- **When**: 检查application.yml
- **Then**: 包含完整的ModelScope配置（API密钥、base-url、模型列表等）
- **Verification**: `programmatic`

### AC-3: ModelProvider包含ModelScope模型
- **Given**: 项目已修改
- **When**: 检查ModelProvider枚举
- **Then**: 包含ModelScope相关的模型定义
- **Verification**: `programmatic`

### AC-4: ModelConfig适配ModelScope
- **Given**: 项目已修改
- **When**: 检查ModelConfig类
- **Then**: 移除了OpenAiChatModel、OpenAiStreamingChatModel相关Bean，添加了ModelScope模型Bean
- **Verification**: `programmatic`

### AC-5: 项目可编译通过
- **Given**: 项目已修改
- **When**: 执行编译
- **Then**: 编译成功，无错误
- **Verification**: `programmatic`

### AC-6: 代码注释完整
- **Given**: 项目已修改
- **When**: 检查新增/修改的代码
- **Then**: 所有类和核心方法都有完整中文注释
- **Verification**: `human-judgment`

## Open Questions
- [ ] ModelScope是否有专门的LangChain4j集成？还是使用OpenAI兼容接口？
