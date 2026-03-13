<template>
  <div class="page">
    <div class="chat-wrapper">

      <!-- 顶部 -->
      <div class="chat-header">
        <div class="title">RAG AI 助手</div>
        <button class="clear-btn" @click="clearChat">清空对话</button>
      </div>

      <!-- 聊天区域 -->
      <div class="chat-body" ref="chatBody">

        <div
          v-for="(msg,index) in messages"
          :key="index"
          class="chat-row"
          :class="msg.role"
        >
          <!-- 头像 -->
          <div class="avatar">
            {{ msg.role === 'user' ? '👤' : '🤖' }}
          </div>

          <!-- 气泡 -->
          <div class="bubble">

            <!-- 用户消息 -->
            <div v-if="msg.role==='user'">
              {{ msg.content }}
            </div>

            <!-- AI消息 -->
            <div v-else>
              <div v-html="renderMarkdown(msg.content || '...')"></div>

              <!-- 引用来源（如果后端返回的话可以展示） -->
              <div
                v-if="msg.sources && msg.sources.length"
                class="sources"
              >
                <div class="sources-title">参考资料：</div>

                <div
                  v-for="(s,i) in msg.sources"
                  :key="i"
                  class="source-item"
                >
                  <b>{{ i + 1 }}. {{ s.title }}</b>
                  <div class="source-content">
                    {{ s.content }}
                  </div>
                </div>

              </div>
            </div>

            <div class="time">{{ msg.time }}</div>

          </div>
        </div>

        <!-- AI思考中（仿 ChatGPT 打字中提示） -->
        <div v-if="loading" class="thinking">
          <span class="thinking-dot"></span>
          <span class="thinking-dot"></span>
          <span class="thinking-dot"></span>
        </div>

      </div>

      <!-- 输入区 -->
      <div class="chat-input">
        <div class="input-row">
          <div class="input-shell">
            <span class="icon-left">+</span>
            <input
              ref="inputRef"
              class="input-textarea"
              v-model="userInput"
              placeholder="给 AI 发送一条消息"
              @keydown="handleKeydown"
              @input="autoResize"
            />
            <span class="icon-mic">🎤</span>
            <button
              class="icon-send"
              :disabled="loading || !userInput.trim()"
              @click="sendQuestion"
            >
              ↑
            </button>
          </div>
        </div>

        <div class="input-actions">
          <div class="hint">Enter 发送，Shift+Enter 换行</div>
          <button
            v-if="loading"
            class="stop-btn"
            @click="stopGenerate"
          >
            停止生成
          </button>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from "vue"

import { marked } from "marked"
import hljs from "highlight.js"
import "highlight.js/styles/github.css"
import { chatRagAndAi } from "@/api/ragController"

const messages = ref([])
const userInput = ref("")
const loading = ref(false)
const chatBody = ref(null)
const inputRef = ref(null)
const currentEventSource = ref(null)

marked.setOptions({
  highlight(code) {
    return hljs.highlightAuto(code).value
  }
})

function renderMarkdown(text) {
  return marked.parse(text || "")
}

function getTime() {
  return new Date().toLocaleTimeString()
}

function scrollBottom() {
  nextTick(() => {
    if (chatBody.value) {
      chatBody.value.scrollTop = chatBody.value.scrollHeight
    }
  })
}

function saveLocal() {
  localStorage.setItem("rag_chat", JSON.stringify(messages.value))
}

function loadLocal() {
  const data = localStorage.getItem("rag_chat")
  if (data) {
    try {
      messages.value = JSON.parse(data)
    } catch {
      messages.value = []
    }
  }
}

// 停止当前回答（关闭 SSE）
function stopGenerate() {
  if (currentEventSource.value) {
    currentEventSource.value.close()
    currentEventSource.value = null
  }
  loading.value = false
  saveLocal()
}

// 发送问题（仿 ChatGPT：先展示用户消息，再流式拼接助手消息）
const sendQuestion = async () => {
  const content = userInput.value.trim()
  if (!content || loading.value) return

  // 追加用户消息
  messages.value.push({
    role: "user",
    content,
    time: getTime()
  })
  saveLocal()
  scrollBottom()

  userInput.value = ""

  // 预先插入一条空的助手消息，用于流式追加
  const assistantMsg = {
    role: "assistant",
    content: "",
    time: getTime()
  }
  messages.value.push(assistantMsg)
  const assistantIndex = messages.value.length - 1

  loading.value = true

  try {
    const eventSource = await chatRagAndAi({
      userQuestion: content
    })

    currentEventSource.value = eventSource

    eventSource.onmessage = (event) => {
      // 如果后端约定有结束标记，可以在这里判断，比如 [DONE]
      if (event.data === "[DONE]") {
        loading.value = false
        eventSource.close()
        currentEventSource.value = null
        saveLocal()
        return
      }

      const msg = messages.value[assistantIndex]
      msg.content += event.data || ""
      // 触发视图更新
      messages.value.splice(assistantIndex, 1, { ...msg })

      scrollBottom()
      saveLocal()
    }

    eventSource.onerror = () => {
      loading.value = false
      if (currentEventSource.value) {
        currentEventSource.value.close()
        currentEventSource.value = null
      }
      saveLocal()
    }

    eventSource.onclose = () => {
      loading.value = false
      if (currentEventSource.value) {
        currentEventSource.value.close()
        currentEventSource.value = null
      }
      saveLocal()
    }
  } catch (e) {
    loading.value = false
  }
}

function handleKeydown(e) {
  if (e.key === "Enter" && !e.shiftKey) {
    e.preventDefault()
    sendQuestion()
  }
}

function autoResize() {
  const el = inputRef.value
  if (!el) return
  el.style.height = "auto"
  el.style.height = `${Math.min(el.scrollHeight, 200)}px`
}

function clearChat() {
  messages.value = []
  saveLocal()
}

onMounted(() => {
  loadLocal()
  scrollBottom()
  if (inputRef.value) {
    inputRef.value.focus()
    autoResize()
  }
})
</script>

<style scoped>
.page {
  height: 100vh;
  background: #f7f7f8;
  display: flex;
  justify-content: center;
  align-items: stretch;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Arial, sans-serif;
}

.chat-wrapper{
  width: 960px;
  margin: 0 auto;
  display:flex;
  flex-direction:column;
  height:100vh;
  padding: 16px 0;
}

.input-textarea::placeholder {
    line-height: 1.5; /* 垂直居中 */
}

.chat-header{
  display:flex;
  justify-content:space-between;
  align-items:center;
  padding: 8px 16px 16px;
}

.title{
  font-size:22px;
  font-weight:600;
}

.clear-btn{
  padding:6px 12px;
  border:none;
  background:#ff4d4f;
  color:white;
  border-radius:6px;
  cursor:pointer;
  font-size: 13px;
}

.chat-body{
  flex:1;
  overflow-y:auto;
  padding: 0 8px 16px;
}

.chat-row{
  display:flex;
  margin-bottom:20px;
  max-width: 840px;
  margin-left: auto;
  margin-right: auto;
  align-items: flex-start;
}

.chat-row.user{
  flex-direction:row-reverse;
}

.avatar{
  width:36px;
  height:36px;
  display:flex;
  align-items:center;
  justify-content:center;
  font-size:18px;
}

.bubble{
  display: inline-block;
  max-width: 75%;
  padding:12px 16px;
  border-radius:12px;
  margin:0 10px;
  position:relative;
  background:#f7f7f8;
}

.user .bubble{
  background:#3b82f6;
  color:white;
}

.assistant .bubble{
  background:#ffffff;
  box-shadow:0 0 0 1px rgba(0,0,0,0.03), 0 2px 6px rgba(0,0,0,0.04);
}

.time{
  font-size:12px;
  margin-top:6px;
  color:#999;
}

.sources{
  margin-top:10px;
  background:#fff;
  border:1px solid #eee;
  padding:10px;
  border-radius:6px;
}

.sources-title{
  font-weight:bold;
  margin-bottom:6px;
}

.source-item{
  margin-bottom:6px;
}

.source-content{
  font-size:13px;
  color:#666;
}

.chat-input{
  border-top:1px solid #e5e5e5;
  padding:12px 16px 20px;
  display:flex;
  flex-direction: column;
  gap:8px;
  background: linear-gradient(to top, #f7f7f8, rgba(247,247,248,0.8));
}

.input-row{
  max-width: 1000px;
  margin: 0 auto;
}

.input-shell{
  display:flex;
  width:800px;
  align-items:center;
  padding: 10px 18px;
  border-radius: 999px;
  background:#ffffff;
  box-shadow:
    0 0 0 1px rgba(0,0,0,0.04),
    0 12px 30px rgba(0,0,0,0.08);
  gap: 8px;
 
}

.icon-left{
  font-size: 18px;
  color:#6b7280;
}

.icon-mic{
  font-size: 16px;
  color:#6b7280;
}

.input-textarea{
  flex:1;
  border:none;
  outline:none;
  resize:none;
  min-height:28px;
  max-height:140px;
  font-size:14px;
  line-height:1.5;
  background:transparent;
}

.input-textarea:focus{
  outline:none;
}

.icon-send{
  width: 32px;
  height: 32px;
  border-radius: 999px;
  border:none;
  cursor:pointer;
  background:#111827;
  color:#ffffff;
  display:flex;
  align-items:center;
  justify-content:center;
  font-size: 16px;
}

.icon-send:disabled{
  opacity:0.4;
  cursor:not-allowed;
}

.input-actions{
  display:flex;
  align-items:center;
  justify-content:space-between;
  max-width: 840px;
  margin: 0 auto;
}

.hint{
  font-size:12px;
  color:#888;
}

.send-btn,
.stop-btn{
  padding: 6px 16px;
  border:none;
  border-radius:999px;
  cursor:pointer;
  font-size: 13px;
}

.send-btn{
  background:#3b82f6;
  color:white;
}

.stop-btn{
  background:#e5e7eb;
  color:#111827;
}

.thinking{
  display:flex;
  gap:4px;
  padding: 8px 16px 16px;
  justify-content:center;
  color:#888;
}

.thinking-dot{
  width:8px;
  height:8px;
  border-radius:50%;
  background:#9ca3af;
  animation: blink 1.4s infinite both;
}

.thinking-dot:nth-child(2){
  animation-delay: 0.2s;
}

.thinking-dot:nth-child(3){
  animation-delay: 0.4s;
}

@keyframes blink {
  0%, 80%, 100% {
    opacity: 0.2;
  }
  40% {
    opacity: 1;
  }
}

pre{
  background:#0f172a;
  color:white;
  padding:10px;
  border-radius:6px;
  overflow:auto;
}

code{
  font-family:monospace;
}
</style>