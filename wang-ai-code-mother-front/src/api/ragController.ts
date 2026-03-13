// SSE 流式对话接口，直接使用 EventSource，避免 axios 拦截器去跳登录
export async function chatRagAndAi(
  params: API.chatRagAndAIParams,
) {
  const { userQuestion } = params
  // 根据实际部署情况选择前缀，这里使用相对路径，走 Vite 代理 /api
  const encoded = encodeURIComponent(userQuestion)
  const url = `/api/rag/chat/${encoded}`

  const es = new EventSource(url)
  return es
}
