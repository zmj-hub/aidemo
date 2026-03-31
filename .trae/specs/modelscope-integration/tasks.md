# ModelScope模型集成 - 实施计划

## [ ] Task 1: 研究ModelScope API接入方式
- **Priority**: P0
- **Depends On**: None
- **Description**:
  - 研究ModelScope是否提供OpenAI兼容API
  - 确定是否需要保留langchain4j-open-ai依赖（用于兼容ModelScope）
  - 确认ModelScope API密钥获取方式
- **Acceptance Criteria Addressed**: [AC-1, AC-2]
- **Test Requirements**:
  - `human-judgement` TR-1.1: 确定ModelScope接入方案

## [ ] Task 2: 修改pom.xml依赖配置
- **Priority**: P0
- **Depends On**: [Task 1]
- **Description**:
  - 根据研究结果，决定是否移除langchain4j-open-ai依赖
  - 如果需要，添加ModelScope相关依赖
  - 清理重复依赖
- **Acceptance Criteria Addressed**: [AC-1]
- **Test Requirements**:
  - `programmatic` TR-2.1: pom.xml依赖配置正确，无冲突

## [ ] Task 3: 创建ModelScope配置属性类
- **Priority**: P0
- **Depends On**: [Task 1]
- **Description**:
  - 创建ModelScopeProperties配置属性类（service/model/properties包下）
  - 包含API密钥、base-url、超时、重试次数、模型列表等配置
- **Acceptance Criteria Addressed**: [AC-2]
- **Test Requirements**:
  - `programmatic` TR-3.1: ModelScopeProperties类创建成功，属性定义完整

## [ ] Task 4: 修改ModelProvider枚举
- **Priority**: P0
- **Depends On**: [Task 1]
- **Description**:
  - 移除Qwen、DeepSeek相关的模型定义
  - 添加ModelScope相关的模型定义（如：qwen-turbo、qwen-max、llama3、qwen2等）
- **Acceptance Criteria Addressed**: [AC-3]
- **Test Requirements**:
  - `programmatic` TR-4.1: ModelProvider枚举包含ModelScope模型定义

## [ ] Task 5: 修改ModelConfig配置类
- **Priority**: P0
- **Depends On**: [Task 3, Task 4]
- **Description**:
  - 移除QwenProperties、DeepSeekProperties的注入
  - 添加ModelScopeProperties的注入
  - 移除所有OpenAiChatModel、OpenAiStreamingChatModel、OpenAiTokenizer相关Bean
  - 添加ModelScope模型的Bean配置（同步和流式）
- **Acceptance Criteria Addressed**: [AC-4]
- **Test Requirements**:
  - `programmatic` TR-5.1: ModelConfig类修改完成，无OpenAI相关代码

## [ ] Task 6: 修改ModelFactory和ChatModelService
- **Priority**: P0
- **Depends On**: [Task 5]
- **Description**:
  - 修改ModelFactory，适配ModelScope模型初始化
  - 修改ChatModelService，适配ModelScope模型调用
  - 确保模型权限控制正常工作
- **Acceptance Criteria Addressed**: [AC-4, AC-5]
- **Test Requirements**:
  - `programmatic` TR-6.1: ModelFactory和ChatModelService适配ModelScope

## [ ] Task 7: 修改application.yml配置
- **Priority**: P0
- **Depends On**: [Task 3]
- **Description**:
  - 移除qwen、deepseek配置段
  - 添加modelscope配置段，包含完整配置示例和注释
  - 更新默认模型配置
- **Acceptance Criteria Addressed**: [AC-2]
- **Test Requirements**:
  - `programmatic` TR-7.1: application.yml包含完整的ModelScope配置

## [ ] Task 8: 编译验证与测试
- **Priority**: P0
- **Depends On**: [Task 2, Task 5, Task 6, Task 7]
- **Description**:
  - 执行mvn clean compile验证编译
  - 检查无诊断错误
  - 验证代码注释完整
- **Acceptance Criteria Addressed**: [AC-5, AC-6]
- **Test Requirements**:
  - `programmatic` TR-8.1: 编译成功，无错误
  - `human-judgement` TR-8.2: 代码注释完整

## [ ] Task 9: 更新README文档
- **Priority**: P1
- **Depends On**: [Task 8]
- **Description**:
  - 更新README中关于API密钥获取的部分，添加ModelScope说明
  - 更新接口调用示例
  - 更新常见问题排查
- **Acceptance Criteria Addressed**: [AC-6]
- **Test Requirements**:
  - `human-judgement` TR-9.1: README文档已更新ModelScope相关内容
