<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { SendOutlined, RocketOutlined, ArrowLeftOutlined } from '@ant-design/icons-vue'
import { getAppVoById, deployApp } from '@/api/appController'
import { useLoginUserStore } from '@/stores/LoginUser'

const route = useRoute()
const router = useRouter()
const loginUserStore = useLoginUserStore()

const appId = ref<number>(Number(route.params.id))
const appInfo = ref<API.AppVO | null>(null)
const loading = ref(false)

const messages = ref<Array<{ role: 'user' | 'assistant'; content: string }>>([])
const inputMessage = ref('')
const sending = ref(false)
const messagesContainer = ref<HTMLElement | null>(null)

const showPreview = ref(false)
const previewUrl = ref('')
const deployLoading = ref(false)
const deployedUrl = ref('')

const fetchAppInfo = async () => {
  loading.value = true
  try {
    const res = await getAppVoById({ id: appId.value })
    if (res.data.code === 0 && res.data.data) {
      appInfo.value = res.data.data
      if (appInfo.value.initPrompt) {
        messages.value.push({
          role: 'user',
          content: appInfo.value.initPrompt
        })
        await sendInitialMessage(appInfo.value.initPrompt)
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
      `http://localhost:8123/api/app/chat/gen/code?appId=${appId.value}&message=${encodeURIComponent(prompt)}`,
      {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        }
      }
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
      content: ''
    })

    while (true) {
      const { done, value } = await reader.read()
      if (done) break

      const chunk = decoder.decode(value)
      const lines = chunk.split('\n')

      for (const line of lines) {
        if (line.startsWith('data: ')) {
          const data = line.slice(6)
          if (data === '[DONE]') {
            showPreview.value = true
            previewUrl.value = `http://localhost:8123/api/static/${appInfo.value?.codeGenType}_${appId.value}/`
            break
          }
          if (data && data !== 'null') {
            messages.value[messages.value.length - 1].content += data
            scrollToBottom()
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
    content: userMessage
  })
  inputMessage.value = ''
  scrollToBottom()

  sending.value = true
  try {
    const response = await fetch(
      `http://localhost:8123/api/app/chat/gen/code?appId=${appId.value}&message=${encodeURIComponent(userMessage)}`,
      {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        }
      }
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
      content: ''
    })

    while (true) {
      const { done, value } = await reader.read()
      if (done) break

      const chunk = decoder.decode(value)
      const lines = chunk.split('\n')

      for (const line of lines) {
        if (line.startsWith('data: ')) {
          const data = line.slice(6)
          if (data === '[DONE]') {
            showPreview.value = true
            previewUrl.value = `http://localhost:8123/api/static/${appInfo.value?.codeGenType}_${appId.value}/`
            break
          }
          if (data && data !== 'null') {
            messages.value[messages.value.length - 1].content += data
            scrollToBottom()
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
})
</script>

<template>
  <div class="app-chat-container">
    <div class="chat-header">
      <div class="header-left">
        <a-button type="text" @click="goBack">
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
                <div class="message-text">
                  {{ msg.content }}
                </div>
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
          <a-input-search
            v-model:value="inputMessage"
            placeholder="输入消息继续对话..."
            size="large"
            :loading="sending"
            @search="handleSendMessage"
          >
            <template #enterButton>
              <a-button type="primary" size="large" :loading="sending">
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
            <iframe
              :src="previewUrl"
              frameborder="0"
              class="preview-frame"
            ></iframe>
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
  height: calc(100vh - 64px - 60px);
  overflow: hidden;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background: #fff;
  border-bottom: 1px solid #f0f0f0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.app-name {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #333;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.chat-content {
  display: flex;
  flex: 1;
  overflow: hidden;
}

.chat-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  border-right: 1px solid #f0f0f0;
  background: #f5f5f5;
}

.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
}

.message-item {
  display: flex;
  margin-bottom: 24px;
}

.user-message {
  justify-content: flex-end;
}

.assistant-message {
  justify-content: flex-start;
}

.message-content {
  max-width: 70%;
}

.user-message .message-content {
  background: #1890ff;
  color: white;
  border-radius: 12px 12px 0 12px;
  padding: 12px 16px;
}

.assistant-message .message-content {
  background: white;
  color: #333;
  border-radius: 12px 12px 12px 0;
  padding: 12px 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.message-role {
  font-size: 12px;
  margin-bottom: 4px;
  opacity: 0.8;
}

.user-message .message-role {
  text-align: right;
}

.message-text {
  font-size: 14px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
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
  0%, 60%, 100% {
    transform: translateY(0);
  }
  30% {
    transform: translateY(-8px);
  }
}

.input-section {
  padding: 16px 24px;
  background: white;
  border-top: 1px solid #f0f0f0;
}

.preview-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: white;
}

.preview-container {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.preview-header {
  padding: 16px 24px;
  border-bottom: 1px solid #f0f0f0;
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
}

.preview-frame {
  width: 100%;
  height: 100%;
  border: none;
}

.preview-placeholder {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
}

:deep(.ant-input-search) {
  width: 100%;
}

:deep(.ant-input-search-button) {
  height: 40px;
}

:deep(.ant-input) {
  height: 40px;
}

:deep(.ant-btn) {
  height: 40px;
}

@media (max-width: 768px) {
  .chat-content {
    flex-direction: column;
  }

  .chat-section {
    flex: 1;
    border-right: none;
    border-bottom: 1px solid #f0f0f0;
  }

  .preview-section {
    display: none;
  }

  .message-content {
    max-width: 85%;
  }
}
</style>
