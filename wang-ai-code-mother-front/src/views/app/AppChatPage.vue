<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { SendOutlined, RocketOutlined, ArrowLeftOutlined } from '@ant-design/icons-vue'
import { getAppVoById, deployApp } from '@/api/appController'
import { useLoginUserStore } from '@/stores/LoginUser'
import { marked } from 'marked'
import { listAppChatHistory } from '@/api/chatHistoryController.ts'

const route = useRoute()
const router = useRouter()
const loginUserStore = useLoginUserStore()

const rawAppId = Array.isArray(route.params.id) ? route.params.id[0] : route.params.id
const appId = ref<string>(rawAppId)
const appInfo = ref<API.AppVO | null>(null)
const loading = ref(false)

const messages = ref<Array<{ role: 'user' | 'assistant'; content: string }>>([])
const inputMessage = ref('')
const messagesContainer = ref<HTMLElement | null>(null)

const renderMarkdown = (content: string) => {
  return marked.parse(content)
}

const showPreview = ref(false)
const previewUrl = ref('')
const deployLoading = ref(false)
const deployedUrl = ref('')

const buildPreviewUrl = () => {
  const codeGenType = appInfo.value?.codeGenType
  if (!codeGenType) return ''
  const indexPath = codeGenType === 'vue_project' ? 'dist/index.html' : 'index.html'
  return `http://localhost:8123/api/static/${codeGenType}_${appId.value}/${indexPath}`
}

const streamChat = async (prompt: string, onAppend: (delta: string) => void, onDone: () => void) => {
  const response = await fetch(
    `http://localhost:8123/api/app/chat/gen/code?appId=${appId.value}&message=${encodeURIComponent(prompt)}`,
    {
      method: 'GET',
      credentials: 'include',
      headers: {
        Authorization: `Bearer ${localStorage.getItem('token')}`,
        Accept: 'text/event-stream',
        'Cache-Control': 'no-cache',
      },
    },
  )

  if (!response.ok) {
    const errorText = await response.text().catch(() => '')
    throw new Error(errorText || `请求失败（${response.status}）`)
  }

  const reader = response.body?.getReader()
  if (!reader) {
    throw new Error('无法读取响应流')
  }

  const decoder = new TextDecoder()
  let buffer = ''
  let currentEventName = 'message'
  let currentDataLines: string[] = []

  const dispatchEvent = () => {
    const data = currentDataLines.join('\n').trim()
    const eventName = currentEventName || 'message'
    currentEventName = 'message'
    currentDataLines = []

    if (eventName === 'done') {
      onDone()
      return
    }

    if (!data || data === 'null' || data === '[DONE]') return

    try {
      const json = JSON.parse(data)
      const delta = json?.d ?? json?.data ?? json?.message ?? ''
      if (delta) onAppend(String(delta))
    } catch {
      onAppend(data)
    }
  }

  while (true) {
    const { done, value } = await reader.read()
    if (done) break

    buffer += decoder.decode(value, { stream: true })
    const parts = buffer.split('\n')
    buffer = parts.pop() ?? ''

    for (const rawLine of parts) {
      const line = rawLine.replace(/\r$/, '')
      if (!line) {
        if (currentDataLines.length > 0 || currentEventName !== 'message') {
          dispatchEvent()
        }
        continue
      }

      if (line.startsWith('event:')) {
        currentEventName = line.replace(/^event:\s*/, '').trim() || 'message'
        continue
      }

      if (line.startsWith('data:')) {
        currentDataLines.push(line.replace(/^data:\s*/, ''))
      }
    }
  }

  if (currentDataLines.length > 0 || currentEventName !== 'message') {
    dispatchEvent()
  }
}

const fetchAppInfo = async () => {
  loading.value = true
  try {
    const res = await getAppVoById({ id: appId.value })
    if (res.data.code === 0 && res.data.data) {
      appInfo.value = res.data.data
      if (appInfo.value.isHistory===true) {
        listAppChatHistory({
          appId: appInfo.value.id ?? appId.value,
          pageSize: 10,
        }).then((res) => {
          const ans = res.data.data?.records || []
          ans.forEach((item) => {
            if (item.messageType === 'user') {
              messages.value.push({
                role: 'user',
                content: item.message ?? '',
              })
            } else if (item.messageType === 'ai') {
              messages.value.push({
                role: 'assistant',
                content: item.message ?? '',
              })
            }
          })
          const url = buildPreviewUrl()
          if (url) previewUrl.value = url
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
  try {
    messages.value.push({ role: 'assistant', content: '' })
    const assistantMessage = messages.value[messages.value.length - 1]!
    await streamChat(
      prompt,
      (delta) => {
        assistantMessage.content += delta
        scrollToBottomImmediate()
      },
      () => {
        showPreview.value = true
        const url = buildPreviewUrl()
        if (url) previewUrl.value = url
      },
    )
  } catch (error) {
    message.error('对话失败，请稍后重试')
    messages.value.pop()
  }
}

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
  messages.value.push({
    role: 'user',
    content: userMessage,
  })
  inputMessage.value = ''
  scrollToBottom()
  scrollToBottomImmediate()

  try {
    messages.value.push({ role: 'assistant', content: '' })
    const assistantMessage = messages.value[messages.value.length - 1]!
    await streamChat(
      userMessage,
      (delta) => {
        assistantMessage.content += delta
        scrollToBottomImmediate()
      },
      () => {
        showPreview.value = true
        const url = buildPreviewUrl()
        if (url) previewUrl.value = url
      },
    )
  } catch (error) {
    message.error('对话失败，请稍后重试')
    messages.value.pop()
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
      // 确保URL格式正确，避免路径拼接错误
      const baseUrl = deployedUrl.value.endsWith('/') ? deployedUrl.value.slice(0, -1) : deployedUrl.value
      window.open(`${baseUrl}/index.html`, '_blank')
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
      </div>
      <div class="header-right">
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
          </div>
        </a-spin>

        <div class="input-section">
          <a-input-search
            v-model:value="inputMessage"
            placeholder="输入消息继续对话..."
            size="large"
            @search="handleSendMessage"
            class="message-input"
          >
            <template #enterButton>
              <a-button type="primary" size="large" class="send-button">
                <template #icon>
                  <SendOutlined />
                </template>
                发送
              </a-button>
            </template>
          </a-input-search>
        </div>
      </div>

      <div class="preview-section">
        <div v-if="showPreview" class="preview-container">
          <div class="preview-header">
            <h3>网站预览</h3>
          </div>
          <div class="preview-iframe">
            <iframe :src="previewUrl" frameborder="0" class="preview-frame"></iframe>
          </div>
        </div>
        <div v-else class="preview-placeholder">
          <a-empty description="网站生成中，请稍候..." />
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
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

.input-section {
  padding: 16px 24px;
  background: #ffffff;
  border-top: 1px solid #e8e8e8;
  position: sticky;
  bottom: 0;
  z-index: 10;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.06);
}

.message-input {
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.send-button {
  border-radius: 0 12px 12px 0;
  background: #1890ff;
  border: none;
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
}
</style>
