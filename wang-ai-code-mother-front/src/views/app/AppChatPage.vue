<script setup lang="ts">
import { ref, onMounted, nextTick, computed, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { SendOutlined, RocketOutlined, ArrowLeftOutlined, DownloadOutlined, InfoCircleOutlined, EditOutlined, CloseOutlined } from '@ant-design/icons-vue'
import { getAppVoById, deployApp, listGoodAppVoByPage, downloadAppCode } from '@/api/appController'
import { useLoginUserStore } from '@/stores/LoginUser'
import { marked } from 'marked'
import { listAppChatHistory } from '@/api/chatHistoryController.ts'
import { VisualEditor, type ElementInfo } from '@/hooks/useVisualEditor'

const route = useRoute()
const router = useRouter()
const loginUserStore = useLoginUserStore()

const appId = ref<string>(String(route.params.id))
const appInfo = ref<API.AppVO | null>(null)
const loading = ref(false)

const messages = ref<Array<{ role: 'user' | 'assistant'; content: string }>>([])
const inputMessage = ref('')
const sending = ref(false)
const messagesContainer = ref<HTMLElement | null>(null)

const renderMarkdown = (content: string) => {
  return marked.parse(content)
}

const showPreview = ref(false)
const previewUrl = ref('')
const deployLoading = ref(false)
const deployedUrl = ref('')

const isEditMode = ref(false)
const selectedElement = ref<ElementInfo | null>(null)
const iframeRef = ref<HTMLIFrameElement | null>(null)
const visualEditor = ref<VisualEditor | null>(null)

const toggleEditMode = () => {
  if (visualEditor.value) {
    isEditMode.value = visualEditor.value.toggleEditMode()
  }
}

const disableEditMode = () => {
  if (visualEditor.value) {
    visualEditor.value.disableEditMode()
    isEditMode.value = false
  }
}

const clearSelection = () => {
  if (visualEditor.value) {
    visualEditor.value.clearSelection()
    selectedElement.value = null
  }
}

const initVisualEditor = () => {
  visualEditor.value = new VisualEditor({
    onElementSelected: (info: ElementInfo) => {
      selectedElement.value = info
    },
    onElementHover: (info: ElementInfo) => {
      // console.log('hover', info)
    }
  })
}

watch(iframeRef, (newVal) => {
  if (newVal && visualEditor.value) {
    visualEditor.value.init(newVal)
  }
})

const onIframeLoad = () => {
  if (visualEditor.value) {
    visualEditor.value.onIframeLoad()
  }
}

const handleIframeMessage = (event: MessageEvent) => {
  if (visualEditor.value) {
    visualEditor.value.handleIframeMessage(event)
  }
}

onMounted(() => {
  initVisualEditor()
  window.addEventListener('message', handleIframeMessage)
})

onUnmounted(() => {
  window.removeEventListener('message', handleIframeMessage)
})

const fetchAppInfo = async () => {
  loading.value = true
  try {
    const res = await getAppVoById({ id: appId.value })
    if (res.data.code === 0 && res.data.data) {
      appInfo.value = res.data.data
      if (appInfo.value.isHistory === true) {
        listAppChatHistory({
          appId: appInfo.value.id,
          current: 1,
          pageSize: 10,
        }).then((res) => {
          const ans = res.data.data?.records || []
          ans.forEach((item) => {
            if (item.messageType === 'user') {
              messages.value.push({
                role: 'user',
                content: item.message,
              })
            } else if (item.messageType === 'ai') {
              messages.value.push({
                role: 'assistant',
                content: item.message,
              })
            }
          })
          getPreViewUrl()
          showPreview.value = true
          scrollToBottom()
        })
      } else {
        if (appInfo.value.initPrompt) {
          messages.value.push({
            role: 'user',
            content: appInfo.value.initPrompt,
          })
          await sendInitialMessage(appInfo.value.initPrompt)
        }
        scrollToBottom()
      }
    } else {
      message.error(res.data.message || '获取应用信息失败')
    }
  } catch (error) {
    message.error('获取应用信息失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const sendInitialMessage = async (prompt: string) => {
  sending.value = true
  try {
    const response = await fetch(
      `http://localhost:5173/api/app/chat/gen/code?appId=${appId.value}&message=${encodeURIComponent(prompt)}`,
      {
        method: 'GET',
        headers: {
          Authorization: `Bearer ${localStorage.getItem('token')}`,
        },
      },
    )

    if (!response.ok) {
      throw new Error('请求失败')
    }

    const reader = response.body?.getReader()
    const decoder = new TextDecoder()

    if (!reader) {
      throw new Error('无法读取响应流')
    }

    messages.value.push({
      role: 'assistant',
      content: '',
    })

    while (true) {
      const { done, value } = await reader.read()
      if (done) break

      const chunk = decoder.decode(value)
      const lines = chunk.split('\n')

      let currentEvent = 'message'

      for (const line of lines) {
        if (line.startsWith('event:')) {
          currentEvent = line.replace(/^event:\s*/, '')
          continue
        }

        if (!line.startsWith('data:')) continue

        const data = line.replace(/^data:\s*/, '')

        if (currentEvent === 'done') {
          showPreview.value = true
          //http://localhost:5173/api/static/html_2011763874621632512/index.html
          getPreViewUrl()
          continue
        }

        if (data && data !== 'null') {
          const json = JSON.parse(data)
          messages.value[messages.value.length - 1].content += json.d
          scrollToBottomImmediate()
        }
      }
    }
  } catch (error) {
    message.error('对话失败，请稍后重试')
    messages.value.pop()
  } finally {
    sending.value = false
  }
}

function getPreViewUrl() {
  if (appInfo.value?.codeGenType === 'html' || appInfo.value?.codeGenType === 'multi_file') {
    previewUrl.value = `http://localhost:5173/api/static/${appInfo.value?.codeGenType}_${appId.value}/index.html`
  } else {
    previewUrl.value = `http://localhost:5173/api/static/${appInfo.value?.codeGenType}_${appId.value}/dist/index.html`
  }
  console.log("previewUrl"+previewUrl.value)
}

/**
 * 初始发送消息
 */
const handleSendMessage = async () => {
  if (!inputMessage.value.trim()) {
    message.warning('请输入消息')
    return
  }

  if (!loginUserStore.isLogin) {
    message.warning('请先登录')
    router.push('/user/login')
    return
  }

  const userMessage = inputMessage.value.trim()
  let prompt = userMessage
  
  if (selectedElement.value) {
    prompt += `\n\n【用户选中元素信息】\n标签: ${selectedElement.value.tagName}\n文本: ${selectedElement.value.textContent}\n选择器: ${selectedElement.value.selector}\n页面路径: ${selectedElement.value.pagePath}`
    if (selectedElement.value.id) prompt += `\nID: ${selectedElement.value.id}`
    if (selectedElement.value.className) prompt += `\nClass: ${selectedElement.value.className}`
  }

  messages.value.push({
    role: 'user',
    content: userMessage, // Display original user message in chat
  })
  inputMessage.value = ''
  scrollToBottom()
  scrollToBottomImmediate()

  // Exit edit mode after sending
  disableEditMode()

  sending.value = true
  try {
    const response = await fetch(
      `http://localhost:5173/api/app/chat/gen/code?appId=${appId.value}&message=${encodeURIComponent(prompt)}`,
      {
        method: 'GET',
        headers: {
          Authorization: `Bearer ${localStorage.getItem('token')}`,
        },
      },
    )

    if (!response.ok) {
      throw new Error('请求失败')
    }

    const reader = response.body?.getReader()
    const decoder = new TextDecoder()

    if (!reader) {
      throw new Error('无法读取响应流')
    }

    messages.value.push({
      role: 'assistant',
      content: '',
    })

    while (true) {
      const { done, value } = await reader.read()
      if (done) break

      const chunk = decoder.decode(value)
      const lines = chunk.split('\n')

      let currentEvent = 'message'

      for (const line of lines) {
        if (line.startsWith('event:')) {
          currentEvent = line.replace(/^event:\s*/, '')
          continue
        }

        if (!line.startsWith('data:')) continue

        const data = line.replace(/^data:\s*/, '')

        if (currentEvent === 'done') {
          showPreview.value = true
          console.log(appInfo.value?.codeGenType)
          //获取不同模式下的预览地址
          getPreViewUrl()
          console.log('previewUrl', previewUrl.value)
          continue
        }

        if (data && data !== 'null') {
          try {
            const json = JSON.parse(data)
            if (json.d) {
              messages.value[messages.value.length - 1].content += json.d
              scrollToBottomImmediate()
            }
          } catch (e) {
            if (data !== '[DONE]') {
              messages.value[messages.value.length - 1].content += data
              scrollToBottomImmediate()
            }
          }
        }
      }
    }
  } catch (error) {
    message.error('对话失败，请稍后重试')
    messages.value.pop()
  } finally {
    sending.value = false
  }
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

const scrollToBottomImmediate = () => {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

const downloadLoading = ref(false)

const handleDownload = async () => {
  if (!loginUserStore.isLogin) {
    message.warning('请先登录')
    router.push('/user/login')
    return
  }

  downloadLoading.value = true
  try {
    const res = await downloadAppCode({ appId: appId.value }, { responseType: 'blob' })
    if (res.data) {
      const blob = new Blob([res.data], { type: 'application/zip' })
      const contentDisposition = res.headers['content-disposition']
      let fileName = 'app_code.zip'
      if (contentDisposition) {
        const fileNameMatch = contentDisposition.match(/filename="?([^"]+)"?/)
        if (fileNameMatch && fileNameMatch.length >= 2) {
          fileName = fileNameMatch[1]
        }
      }
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = fileName
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      window.URL.revokeObjectURL(url)
      message.success('下载成功')
    } else {
      message.error('下载失败')
    }
  } catch (error) {
    message.error('下载失败，请稍后重试')
  } finally {
    downloadLoading.value = false
  }
}

const showDetail = ref(false)
const showDetailModal = () => {
  showDetail.value = true
}

const getCodeGenTypeLabel = (codeGenType?: string) => {
  if (codeGenType === 'html') {
    return 'HTML 网站'
  } else if (codeGenType === 'multi_file') {
    return '多文件应用'
  }
  return codeGenType
}

const formatDate = (isoString?: string) => {
  if (!isoString) return ''
  const date = new Date(isoString)
  const year = date.getFullYear()
  const month = (date.getMonth() + 1).toString().padStart(2, '0')
  const day = date.getDate().toString().padStart(2, '0')
  const hours = date.getHours().toString().padStart(2, '0')
  const minutes = date.getMinutes().toString().padStart(2, '0')
  const seconds = date.getSeconds().toString().padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

const handleDeploy = async () => {
  if (!loginUserStore.isLogin) {
    message.warning('请先登录')
    router.push('/user/login')
    return
  }

  deployLoading.value = true
  try {
    const res = await deployApp({ appId: appId.value })
    if (res.data.code === 0 && res.data.data) {
      message.success('部署成功')
      deployedUrl.value = res.data.data
      window.open(deployedUrl.value, '_blank')
    } else {
      message.error(res.data.message || '部署失败')
    }
  } catch (error) {
    message.error('部署失败，请稍后重试')
  } finally {
    deployLoading.value = false
  }
}

const goBack = () => {
  router.push('/')
}

onMounted(() => {
  fetchAppInfo()
  setTimeout(() => {
    scrollToBottom()
  }, 500)
})
</script>

<template>
  <div class="app-chat-container">
    <div class="chat-header">
      <div class="header-left">
        <a-button type="text" @click="goBack" class="back-button">
          <template #icon>
            <ArrowLeftOutlined />
          </template>
        </a-button>
        <h2 class="app-name">{{ appInfo?.appName || '未命名应用' }}</h2>
        <a-tag v-if="appInfo?.codeGenType" color="blue" style="margin-left: 12px">
          {{ getCodeGenTypeLabel(appInfo.codeGenType) }}
        </a-tag>
      </div>
      <div class="header-right">
        <a-button type="text" @click="showDetailModal" class="detail-button" style="margin-right: 12px">
          <template #icon>
            <InfoCircleOutlined />
          </template>
        </a-button>
        <a-button
          class="download-button"
          :loading="downloadLoading"
          @click="handleDownload"
          style="margin-right: 12px"
        >
          <template #icon>
            <DownloadOutlined />
          </template>
          下载代码
        </a-button>
        <a-button
          type="primary"
          :loading="deployLoading"
          @click="handleDeploy"
          class="deploy-button"
        >
          <template #icon>
            <RocketOutlined />
          </template>
          部署
        </a-button>
      </div>
    </div>

    <div class="chat-content">
      <div class="chat-section">
        <a-spin :spinning="loading">
          <div ref="messagesContainer" class="messages-container">
            <div
              v-for="(msg, index) in messages"
              :key="index"
              :class="['message-item', msg.role === 'user' ? 'user-message' : 'assistant-message']"
            >
              <div class="message-content">
                <div class="message-role">
                  {{ msg.role === 'user' ? '你' : 'AI' }}
                </div>
                <div class="message-text" v-html="renderMarkdown(msg.content)"></div>
              </div>
            </div>
            <div v-if="sending" class="message-item assistant-message">
              <div class="message-content">
                <div class="message-role">AI</div>
                <div class="message-text typing">
                  <span class="typing-dot"></span>
                  <span class="typing-dot"></span>
                  <span class="typing-dot"></span>
                </div>
              </div>
            </div>
          </div>
        </a-spin>

        <div class="input-section">
          <div v-if="selectedElement" class="selected-element-alert">
            <a-alert
              type="info"
              show-icon
              closable
              @close="clearSelection"
            >
              <template #message>
                <span class="alert-title">已选中元素: &lt;{{ selectedElement.tagName }}&gt;</span>
              </template>
              <template #description>
                <div class="alert-desc">
                  文本: {{ selectedElement.textContent }}
                </div>
              </template>
            </a-alert>
          </div>
          <div class="input-wrapper">
            <a-textarea
              v-model:value="inputMessage"
              placeholder="输入消息继续对话..."
              :auto-size="{ minRows: 1, maxRows: 4 }"
              class="custom-textarea"
              @keydown.enter.prevent="handleSendMessage"
            />
            <div class="action-buttons">
              <a-tooltip :title="isEditMode ? '退出编辑模式' : '进入编辑模式'">
                <a-button 
                  :type="isEditMode ? 'primary' : 'default'" 
                  :danger="isEditMode"
                  class="action-btn edit-btn" 
                  @click="toggleEditMode"
                >
                  <template #icon>
                    <EditOutlined />
                  </template>
                </a-button>
              </a-tooltip>
              <a-button 
                type="primary" 
                :loading="sending" 
                class="action-btn send-btn"
                @click="handleSendMessage"
              >
                <template #icon>
                  <SendOutlined />
                </template>
                发送
              </a-button>
            </div>
          </div>
        </div>
      </div>

      <div class="preview-section">
        <div v-if="showPreview" class="preview-container">
          <div class="preview-header">
            <h3>网站预览</h3>
          </div>
          <div class="preview-iframe">
            <iframe 
              ref="iframeRef"
              :src="previewUrl" 
              frameborder="0" 
              class="preview-frame"
              @load="onIframeLoad"
            ></iframe>
          </div>
        </div>
        <div v-else class="preview-placeholder">
          <a-empty description="网站生成中，请稍候..." />
        </div>
      </div>
    </div>

    <a-modal v-model:open="showDetail" title="应用详情" :footer="null" width="800px">
      <a-descriptions bordered :column="{ xs: 1, sm: 2, md: 2 }">
        <a-descriptions-item label="应用名称">
          {{ appInfo?.appName }}
        </a-descriptions-item>
        <a-descriptions-item label="生成类型">
          <a-tag v-if="appInfo?.codeGenType" color="blue">
            {{ getCodeGenTypeLabel(appInfo.codeGenType) }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="创建时间">
          {{ formatDate(appInfo?.createTime) }}
        </a-descriptions-item>
        <a-descriptions-item label="更新时间">
          {{ formatDate(appInfo?.updateTime) }}
        </a-descriptions-item>
        <a-descriptions-item label="初始提示词" :span="2" v-if="appInfo?.initPrompt">
          <div class="prompt-content">
            {{ appInfo.initPrompt }}
          </div>
        </a-descriptions-item>
      </a-descriptions>
    </a-modal>
  </div>
</template>

<style scoped>
.prompt-content {
  max-height: 300px;
  overflow-y: auto;
  white-space: pre-wrap;
  background: #f5f5f5;
  padding: 12px;
  border-radius: 8px;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 14px;
  color: #333;
}

.app-chat-container {
  display: flex;
  flex-direction: column;
  min-height: calc(100vh - 64px);
  background: #f7f8fa;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background: #ffffff;
  border-bottom: 1px solid #e8e8e8;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.back-button {
  color: #666;
  font-size: 16px;
}

.app-name {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #333;
  font-family:
    -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.download-button {
  border-radius: 8px;
  font-weight: 600;
}

.deploy-button {
  background: #1890ff;
  border: none;
  border-radius: 8px;
  font-weight: 600;
}

.chat-content {
  display: flex;
  flex: 1;
  overflow: hidden;
}

.chat-section {
  flex: 3;
  display: flex;
  flex-direction: column;
  background: #ffffff;
  border-right: 1px solid #e8e8e8;
  height: calc(100vh - 128px);
  overflow: hidden;
}

.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  scroll-behavior: smooth;
  background: #ffffff;
  scrollbar-width: thin;
  scrollbar-color: #e0e0e0 #f5f5f5;
  max-height: calc(100vh - 220px);
}

.messages-container::-webkit-scrollbar {
  width: 6px;
}

.messages-container::-webkit-scrollbar-track {
  background: #f5f5f5;
  border-radius: 3px;
}

.messages-container::-webkit-scrollbar-thumb {
  background: #e0e0e0;
  border-radius: 3px;
}

.messages-container::-webkit-scrollbar-thumb:hover {
  background: #bdbdbd;
}

.message-item {
  display: flex;
  margin-bottom: 24px;
  gap: 12px;
}

.user-message {
  justify-content: flex-end;
}

.assistant-message {
  justify-content: flex-start;
}

.message-content {
  max-width: 70%;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.message-role {
  font-size: 12px;
  font-weight: 600;
  color: #999;
  margin-bottom: 4px;
}

.user-message .message-role {
  text-align: right;
  color: #1890ff;
}

.assistant-message .message-role {
  text-align: left;
  color: #fa8c16;
}

.message-text {
  padding: 12px 16px;
  border-radius: 12px;
  line-height: 1.6;
  font-size: 14px;
  word-wrap: break-word;
  white-space: pre-wrap;
  overflow-wrap: break-word;
  word-break: break-all;
  min-height: 40px;
}

.message-text pre {
  background: #f5f5f5;
  padding: 12px;
  border-radius: 8px;
  overflow-x: auto;
  margin: 8px 0;
}

.message-text code {
  background: #f5f5f5;
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
}

.message-text h1,
.message-text h2,
.message-text h3,
.message-text h4,
.message-text h5,
.message-text h6 {
  margin: 12px 0;
  font-weight: 600;
}

.message-text ul,
.message-text ol {
  margin: 8px 0;
  padding-left: 24px;
}

.message-text li {
  margin: 4px 0;
}

.message-text blockquote {
  border-left: 4px solid #1890ff;
  padding-left: 12px;
  margin: 8px 0;
  color: #666;
  font-style: italic;
}

.message-text table {
  border-collapse: collapse;
  width: 100%;
  margin: 12px 0;
}

.message-text th,
.message-text td {
  border: 1px solid #e8e8e8;
  padding: 8px 12px;
  text-align: left;
}

.message-text th {
  background: #fafafa;
  font-weight: 600;
}

.user-message .message-text {
  background: #e6f7ff;
  color: #1890ff;
  border-bottom-right-radius: 4px;
}

.assistant-message .message-text {
  background: #fafafa;
  color: #333;
  border-bottom-left-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.typing {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 8px 0;
}

.typing-dot {
  width: 8px;
  height: 8px;
  background: currentColor;
  border-radius: 50%;
  animation: typing 1.4s infinite ease-in-out;
}

.typing-dot:nth-child(1) {
  animation-delay: 0s;
}

.typing-dot:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-dot:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes typing {
  0%,
  60%,
  100% {
    transform: translateY(0);
  }
  30% {
    transform: translateY(-8px);
  }
}

.input-section {
  position: sticky;
  bottom: 0;
  z-index: 10;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.06);
  display: flex;
  flex-direction: column;
  gap: 12px;
  background: #ffffff;
  padding: 16px 24px;
  border-top: 1px solid #e8e8e8;
}

.selected-element-alert {
  margin-bottom: 8px;
}

.alert-title {
  font-weight: 600;
  font-size: 14px;
}

.alert-desc {
  font-size: 12px;
  color: #666;
  max-height: 40px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.input-wrapper {
  display: flex;
  align-items: flex-end;
  gap: 12px;
  background: #f5f5f5;
  padding: 8px;
  border-radius: 12px;
}

.custom-textarea {
  flex: 1;
  background: transparent;
  border: none;
  box-shadow: none;
  padding: 8px 12px;
  font-size: 14px;
  resize: none;
}

.custom-textarea:focus {
  box-shadow: none;
}

.action-buttons {
  display: flex;
  gap: 8px;
  padding-bottom: 4px;
}

.action-btn {
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.edit-btn {
  width: 32px;
  height: 32px;
  padding: 0;
}

.send-btn {
  padding: 0 16px;
  height: 32px;
  font-weight: 600;
}

.preview-section {
  flex: 2;
  display: flex;
  flex-direction: column;
  background: #ffffff;
  border-left: 1px solid #e8e8e8;
  height: calc(100vh - 128px);
  overflow: hidden;
}

.preview-container {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.preview-header {
  padding: 16px 24px;
  background: #ffffff;
  border-bottom: 1px solid #e8e8e8;
}

.preview-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.preview-iframe {
  flex: 1;
  overflow: hidden;
  padding: 16px;
}

.preview-frame {
  width: 100%;
  height: 100%;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.preview-placeholder {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
}

@media (max-width: 768px) {
  .chat-content {
    flex-direction: column;
  }

  .chat-section {
    flex: 1;
    border-right: none;
    border-bottom: 1px solid #e8e8e8;
  }

  .preview-section {
    flex: 1;
    border-left: none;
    border-top: 1px solid #e8e8e8;
  }

  .input-section {
    position: fixed;
    bottom: 0;
    left: 0;
    right: 0;
    background: #ffffff;
    border-top: 1px solid #e8e8e8;
  }
}
</style>
