<template>
  <div class="model-view">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>模型管理</h2>
      <p>配置和管理 AI 模型，支持多种大语言模型接入</p>
    </div>

    <!-- 操作栏 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          添加模型
        </el-button>
        <el-button
          type="success"
          :loading="batchChecking"
          :disabled="models.length === 0"
          @click="handleBatchHealthCheck"
        >
          <el-icon><CircleCheck /></el-icon>
          全部检查
        </el-button>
      </div>
      <div class="toolbar-right">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索模型..."
          style="width: 240px;"
          clearable
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="3" animated />
      <el-skeleton :rows="3" animated style="margin-top: 20px;" />
      <el-skeleton :rows="3" animated style="margin-top: 20px;" />
    </div>

    <!-- 模型卡片列表 -->
    <div v-else class="model-grid">
      <el-card
        v-for="model in filteredModels"
        :key="model.id"
        class="model-card"
        shadow="hover"
      >
        <div class="card-header">
          <div class="model-info">
            <div class="model-icon-wrapper">
              <el-icon :size="28" color="#1890ff"><Cpu /></el-icon>
              <span
                class="status-dot"
                :class="{
                  'status-healthy': model.healthStatus === 'healthy',
                  'status-unhealthy': model.healthStatus === 'unhealthy',
                  'status-checking': model.healthStatus === 'checking',
                  'status-unknown': model.healthStatus === 'unknown' || !model.healthStatus
                }"
              ></span>
            </div>
            <div class="model-text-info">
              <h3>{{ model.name }}</h3>
              <div class="model-meta">
                <el-tag :type="model.enabled ? 'success' : 'info'" size="small">
                  {{ model.enabled ? '已启用' : '已禁用' }}
                </el-tag>
                <span class="provider-name">{{ model.provider }}</span>
              </div>
            </div>
          </div>
          <el-dropdown trigger="click" @command="(cmd) => handleCommand(cmd, model)">
            <el-button link>
              <el-icon><MoreFilled /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="edit">编辑</el-dropdown-item>
                <el-dropdown-item command="healthCheck">健康检查</el-dropdown-item>
                <el-dropdown-item command="toggle">
                  {{ model.enabled ? '禁用' : '启用' }}
                </el-dropdown-item>
                <el-dropdown-item command="delete" divided>删除</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>

        <div class="card-body">
          <div class="info-row">
            <span class="label">模型ID：</span>
            <span class="value">{{ model.modelId || '-' }}</span>
          </div>
          <div class="info-row">
            <span class="label">API端点：</span>
            <span class="value endpoint-value">{{ model.endpoint || '-' }}</span>
          </div>
          <div class="info-row">
            <span class="label">最大Token：</span>
            <span class="value">{{ model.maxTokens || '-' }}</span>
          </div>
          <div class="info-row">
            <span class="label">温度：</span>
            <span class="value">{{ model.temperature ?? '-' }}</span>
          </div>

          <!-- 健康状态显示区域 -->
          <div class="health-status-section">
            <div class="health-status-header">
              <span class="label">健康状态：</span>
              <el-tag
                v-if="model.healthStatus === 'healthy'"
                type="success"
                size="small"
                effect="dark"
              >
                <el-icon><CircleCheckFilled /></el-icon>
                健康
              </el-tag>
              <el-tag
                v-else-if="model.healthStatus === 'unhealthy'"
                type="danger"
                size="small"
                effect="dark"
              >
                <el-icon><CircleCloseFilled /></el-icon>
                异常
              </el-tag>
              <el-tag
                v-else-if="model.healthStatus === 'checking'"
                type="warning"
                size="small"
                effect="dark"
              >
                <el-icon class="is-loading"><Loading /></el-icon>
                检测中...
              </el-tag>
              <el-tag
                v-else
                type="info"
                size="small"
              >
                未检测
              </el-tag>
            </div>
            <div v-if="model.healthMessage" class="health-message">
              {{ model.healthMessage }}
            </div>
            <div v-if="model.lastCheckTime" class="last-check-time">
              上次检测：{{ formatDateTime(model.lastCheckTime) }}
            </div>
          </div>
        </div>

        <div class="card-footer">
          <div class="footer-left">
            <span class="update-time">更新于 {{ formatDate(model.updatedAt) }}</span>
          </div>
          <div class="footer-right">
            <el-button
              type="primary"
              size="small"
              :loading="model.healthStatus === 'checking'"
              @click="handleSingleHealthCheck(model)"
            >
              <el-icon><CircleCheck /></el-icon>
              检查健康
            </el-button>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 空状态 -->
    <el-empty v-if="!loading && filteredModels.length === 0" description="暂无模型配置">
      <el-button type="primary" @click="handleAdd">添加模型</el-button>
    </el-empty>

    <!-- 编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑模型' : '添加模型'"
      width="600px"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="模型名称" prop="name">
          <el-input v-model="formData.name" placeholder="例如：GPT-4" />
        </el-form-item>

        <el-form-item label="提供商" prop="provider">
          <el-select v-model="formData.provider" placeholder="选择提供商" style="width: 100%;">
            <el-option label="OpenAI" value="OpenAI" />
            <el-option label="Anthropic" value="Anthropic" />
            <el-option label="Google" value="Google" />
            <el-option label="Azure" value="Azure" />
            <el-option label="自定义" value="Custom" />
          </el-select>
        </el-form-item>

        <el-form-item label="API端点" prop="endpoint">
          <el-input v-model="formData.endpoint" placeholder="https://api.openai.com/v1" />
        </el-form-item>

        <el-form-item label="API密钥" prop="apiKey">
          <el-input v-model="formData.apiKey" type="password" show-password placeholder="sk-..." />
        </el-form-item>

        <el-form-item label="模型ID" prop="modelId">
          <el-input v-model="formData.modelId" placeholder="gpt-4 / claude-3-opus" />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="最大Token" prop="maxTokens">
              <el-input-number v-model="formData.maxTokens" :min="1" :max="128000" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="温度" prop="temperature">
              <el-slider v-model="formData.temperature" :min="0" :max="2" :step="0.1" show-input />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="启用">
          <el-switch v-model="formData.enabled" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { formatDate, formatDateTime } from '@/utils/index'
import { getModelList, healthCheck, batchHealthCheck } from '@/api/model'

// 响应式数据
const loading = ref(false)
const submitting = ref(false)
const batchChecking = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const searchKeyword = ref('')
const formRef = ref(null)

// 数据列表
const models = ref([])

// 表单数据
const formData = ref({
  name: '',
  provider: '',
  endpoint: '',
  apiKey: '',
  modelId: '',
  maxTokens: 4096,
  temperature: 0.7,
  enabled: true
})

// 表单验证规则
const formRules = {
  name: [{ required: true, message: '请输入模型名称', trigger: 'blur' }],
  provider: [{ required: true, message: '请选择提供商', trigger: 'change' }],
  endpoint: [{ required: true, message: '请输入API端点', trigger: 'blur' }],
  apiKey: [{ required: true, message: '请输入API密钥', trigger: 'blur' }],
  modelId: [{ required: true, message: '请输入模型ID', trigger: 'blur' }]
}

// 过滤后的模型列表
const filteredModels = computed(() => {
  if (!searchKeyword.value) return models.value
  return models.value.filter(model =>
    model.name.toLowerCase().includes(searchKeyword.value.toLowerCase()) ||
    (model.provider && model.provider.toLowerCase().includes(searchKeyword.value.toLowerCase()))
  )
})

// 获取模型列表
async function fetchModels() {
  loading.value = true
  try {
    const res = await getModelList()
    if (res.data && Array.isArray(res.data)) {
      models.value = res.data.map(model => ({
        ...model,
        healthStatus: model.healthStatus || 'unknown',
        healthMessage: model.healthMessage || '',
        lastCheckTime: model.lastCheckTime || null
      }))
    } else {
      console.warn('API返回数据格式异常:', res)
      models.value = []
    }
  } catch (error) {
    console.error('获取模型列表失败:', error)
    ElMessage.error('获取模型列表失败')
    models.value = []
  } finally {
    loading.value = false
  }
}

// 单模型健康检查
async function handleSingleHealthCheck(model) {
  // 设置为检测中状态
  const index = models.value.findIndex(m => m.id === model.id)
  if (index !== -1) {
    models.value[index].healthStatus = 'checking'
    models.value[index].healthMessage = ''
  }

  try {
    const res = await healthCheck(model.code || model.id)

    if (index !== -1) {
      if (res.data && res.data.success) {
        models.value[index].healthStatus = 'healthy'
        models.value[index].healthMessage = res.data.message || '连接正常'
        ElMessage.success(`${model.name} 健康检查通过`)
      } else {
        models.value[index].healthStatus = 'unhealthy'
        models.value[index].healthMessage = res.data?.message || res.data?.error || '连接异常'
        ElMessage.warning(`${model.name} 健康检查未通过`)
      }
      models.value[index].lastCheckTime = new Date().toISOString()
    }
  } catch (error) {
    console.error(`模型 ${model.name} 健康检查失败:`, error)
    if (index !== -1) {
      models.value[index].healthStatus = 'unhealthy'
      models.value[index].healthMessage = error.response?.data?.message || error.message || '检查请求失败'
      models.value[index].lastCheckTime = new Date().toISOString()
    }
    ElMessage.error(`${model.name} 健康检查失败`)
  }
}

// 批量健康检查
async function handleBatchHealthCheck() {
  if (models.value.length === 0) {
    ElMessage.warning('没有可检查的模型')
    return
  }

  batchChecking.value = true

  // 将所有模型设置为检测中状态
  models.value.forEach(model => {
    model.healthStatus = 'checking'
    model.healthMessage = ''
  })

  try {
    const modelCodes = models.value.map(m => m.code || m.id).filter(Boolean)
    const res = await batchHealthCheck({ modelCodes })

    if (res.data && typeof res.data === 'object') {
      // 处理批量检查结果（假设返回格式为 { [modelCode]: { success, message } }）
      Object.keys(res.data).forEach(code => {
        const index = models.value.findIndex(m =>
          (m.code || m.id).toString() === code.toString()
        )

        if (index !== -1) {
          const result = res.data[code]
          if (result.success) {
            models.value[index].healthStatus = 'healthy'
            models.value[index].healthMessage = result.message || '连接正常'
          } else {
            models.value[index].healthStatus = 'unhealthy'
            models.value[index].healthMessage = result.message || result.error || '连接异常'
          }
          models.value[index].lastCheckTime = new Date().toISOString()
        }
      })
      ElMessage.success('批量健康检查完成')
    } else {
      // 如果返回格式不同，尝试逐个检查或使用备用方案
      console.warn('批量检查返回格式异常，尝试逐个检查')
      for (const model of models.value) {
        await handleSingleHealthCheck(model)
      }
    }
  } catch (error) {
    console.error('批量健康检查失败:', error)
    ElMessage.error('批量健康检查失败，已回退为逐个检查')

    // 回退方案：逐个检查
    for (const model of models.value) {
      if (model.healthStatus === 'checking') {
        await handleSingleHealthCheck(model)
      }
    }
  } finally {
    batchChecking.value = false
  }
}

// 添加模型
function handleAdd() {
  isEdit.value = false
  formData.value = {
    name: '',
    provider: '',
    endpoint: '',
    apiKey: '',
    modelId: '',
    maxTokens: 4096,
    temperature: 0.7,
    enabled: true
  }
  dialogVisible.value = true
}

// 编辑模型
function handleEdit(model) {
  isEdit.value = true
  formData.value = { ...model, apiKey: '' }
  dialogVisible.value = true
}

// 处理下拉菜单命令
function handleCommand(command, model) {
  switch (command) {
    case 'edit':
      handleEdit(model)
      break
    case 'healthCheck':
      handleSingleHealthCheck(model)
      break
    case 'toggle':
      handleToggle(model)
      break
    case 'delete':
      handleDelete(model)
      break
  }
}

// 启用/禁用
async function handleToggle(model) {
  try {
    // TODO: 调用实际API
    // await updateModel(model.id, { enabled: !model.enabled })
    model.enabled = !model.enabled
    ElMessage.success(`${model.name} 已${model.enabled ? '启用' : '禁用'}`)
  } catch (error) {
    console.error('操作失败:', error)
  }
}

// 删除模型
async function handleDelete(model) {
  try {
    await ElMessageBox.confirm(
      `确定要删除模型"${model.name}"吗？`,
      '提示',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )

    // TODO: 调用实际API
    // await deleteModel(model.id)

    models.value = models.value.filter(m => m.id !== model.id)
    ElMessage.success('删除成功')
  } catch (error) {
    if (error !== 'cancel') console.error('删除失败:', error)
  }
}

// 提交表单
async function handleSubmit() {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        if (isEdit.value) {
          // TODO: 调用实际API
          // await updateModel(formData.value.id, formData.value)
          ElMessage.success('更新成功')
        } else {
          // TODO: 调用实际API
          // const res = await createModel(formData.value)
          // models.value.push(res)
          ElMessage.success('添加成功')
        }
        dialogVisible.value = false
        fetchModels()
      } catch (error) {
        console.error('保存失败:', error)
      } finally {
        submitting.value = false
      }
    }
  })
}

onMounted(() => {
  fetchModels()
})
</script>

<style scoped>
.model-view {
  padding: 24px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 120px);
}

.page-header {
  margin-bottom: 24px;
}

.page-header h2 {
  font-size: 28px;
  color: #1a1a1a;
  margin-bottom: 8px;
  font-weight: 600;
}

.page-header p {
  color: #666;
  font-size: 14px;
  line-height: 1.6;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 16px 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.toolbar-left {
  display: flex;
  gap: 12px;
}

.toolbar-right {
  display: flex;
  gap: 12px;
}

/* 加载容器 - 增强版 */
.loading-container {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
  gap: 24px;
  padding: 24px;
  background: white;
  border-radius: 16px;
  animation: loading-pulse 2s ease-in-out infinite;
}

@keyframes loading-pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.8; }
}

.model-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(420px, 1fr));
  gap: 20px;
}

.model-card {
  border-radius: 16px;
  transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
  border: 1px solid #e8ecf0;
  position: relative;
  overflow: hidden;
}

/* 卡片顶部装饰线 */
.model-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, transparent, #1890ff, transparent);
  opacity: 0;
  transition: opacity var(--transition-normal);
}

.model-card:hover {
  transform: translateY(-6px) scale(1.01);
  box-shadow:
    0 12px 28px rgba(0, 0, 0, 0.1),
    0 4px 12px rgba(24, 144, 255, 0.15);
  border-color: #1890ff;
}

.model-card:hover::before {
  opacity: 1;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.model-info {
  display: flex;
  gap: 14px;
  align-items: center;
  flex: 1;
}

.model-icon-wrapper {
  position: relative;
  width: 48px;
  height: 48px;
  border-radius: 10px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.model-icon-wrapper .el-icon {
  color: white !important;
}

.status-dot {
  position: absolute;
  bottom: -2px;
  right: -2px;
  width: 14px;
  height: 14px;
  border-radius: 50%;
  border: 2px solid white;
  transition: all 0.3s ease;
}

.status-dot.status-healthy {
  background-color: #52c41a;
  box-shadow: 0 0 8px rgba(82, 196, 26, 0.5);
}

.status-dot.status-unhealthy {
  background-color: #ff4d4f;
  box-shadow: 0 0 8px rgba(255, 77, 79, 0.5);
}

.status-dot.status-checking {
  background-color: #faad14;
  animation: pulse 1.5s infinite;
}

.status-dot.status-unknown {
  background-color: #d9d9d9;
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.7;
    transform: scale(1.1);
  }
}

.model-text-info {
  flex: 1;
  min-width: 0;
}

.model-text-info h3 {
  margin: 0 0 6px 0;
  font-size: 18px;
  color: #1a1a1a;
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.model-meta {
  display: flex;
  align-items: center;
  gap: 8px;
}

.provider-name {
  font-size: 13px;
  color: #666;
}

.card-body {
  margin-bottom: 16px;
}

.info-row {
  display: flex;
  margin-bottom: 10px;
  font-size: 13px;
  line-height: 1.6;
}

.info-row .label {
  color: #999;
  width: 85px;
  flex-shrink: 0;
  font-weight: 500;
}

.info-row .value {
  color: #333;
  word-break: break-all;
  flex: 1;
}

.endpoint-value {
  font-family: 'Monaco', 'Menlo', monospace;
  font-size: 12px;
  color: #666;
}

/* 健康状态区域样式 */
.health-status-section {
  margin-top: 16px;
  padding: 12px;
  background: #fafbfc;
  border-radius: 8px;
  border-left: 3px solid #1890ff;
}

.health-status-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.health-status-header .label {
  color: #666;
  font-size: 13px;
  font-weight: 500;
}

.health-message {
  font-size: 12px;
  color: #666;
  margin-bottom: 4px;
  line-height: 1.5;
  word-break: break-word;
}

.last-check-time {
  font-size: 11px;
  color: #999;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 14px;
  border-top: 1px solid #f0f0f0;
}

.footer-left {
  flex: 1;
}

.update-time {
  font-size: 12px;
  color: #999;
}

.footer-right {
  flex-shrink: 0;
}

/* 响应式适配 */
@media (max-width: 1200px) {
  .model-grid {
    grid-template-columns: repeat(auto-fill, minmax(360px, 1fr));
  }
}

@media (max-width: 768px) {
  .model-view {
    padding: 16px;
  }

  .toolbar {
    flex-direction: column;
    gap: 12px;
  }

  .toolbar-left,
  .toolbar-right {
    width: 100%;
  }

  .toolbar-right .el-input {
    width: 100% !important;
  }

  .model-grid {
    grid-template-columns: 1fr;
  }

  .page-header h2 {
    font-size: 24px;
  }
}
</style>
