<script setup lang="ts">
import {ref, onMounted, nextTick} from 'vue'
import {useRouter} from 'vue-router'
import {message} from 'ant-design-vue'
import {
  PlusOutlined,
  ArrowUpOutlined,
  SearchOutlined,
  ReloadOutlined,
  ThunderboltOutlined,
  ShoppingOutlined,
  FileTextOutlined,
  BankOutlined,
  BarChartOutlined,
  StarOutlined,
  RightOutlined,
  AppstoreOutlined,
  SettingOutlined,
  UserOutlined,
} from '@ant-design/icons-vue'
import {addApp, listMyAppVoByPage, listGoodAppVoByPage} from '@/api/appController'
import {useLoginUserStore} from '@/stores/LoginUser'

const router = useRouter()
const loginUserStore = useLoginUserStore()

const promptInput = ref('')
const createLoading = ref(false)

const examplePrompts = [
  {
    prompt:
      '设计一个复古波普艺术风格的电商网页，背景使用鲜艳的橙色和粉色撞色，融入漫画风格的圆点图案和手绘插图' +
      '。商品展示区以波普画框形式呈现，按钮带有手写字体和跳跃动画。整体风格活泼有趣，适合潮流服饰或艺术品销售。',
  },
  {
    prompt: '创建一个企业网站，风格要大气、商务、专业。设计一个完整的企业网站首页，包括导航栏、hero区域、服务介绍、公司优势、客户评价等部分。',
  },
  {
    prompt: '设计一个现代化电商运营后台，背景为纯白，顶部导航使用渐变主题色(#4F46E5 → #2563EB)。订单处理采用卡片流设计，' +
      '商品管理支持拖拽排序。数据分析区使用动态仪表盘，支持多维度筛选。整体风格简洁高效。',
  },
  {
    prompt: '参考微博的能力和布局生成一个暗黑色调的话题社区',
  },
]

const myAppsLoading = ref(false)
const myApps = ref<API.AppVO[]>([])
const myAppsTotal = ref(0)
const myAppsCurrentPage = ref(1)
const myAppsPageSize = ref(20)
const myAppsSearchName = ref('')

const goodAppsLoading = ref(false)
const goodApps = ref<API.AppVO[]>([])
const goodAppsTotal = ref(0)
const goodAppsCurrentPage = ref(1)
const goodAppsPageSize = ref(20)
const goodAppsSearchName = ref('')

const realPlaceholder = ref(
  '使用 AI零代码生成平台 创建一个数据分析看板，用来分析销售数据、用户增长趋势...',
)

const typePlaceholder = ref('')
let index = 0

function typePlaceholderStart() {
  const text = realPlaceholder.value
  const speed = 100 // 打字速度

  const timer = setInterval(() => {
    if (index < text.length) {
      typePlaceholder.value = text.slice(0, index + 1)
      index++
    } else {
      clearInterval(timer)
    }
  }, speed)
}

const handleCreateApp = async () => {
  if (!promptInput.value.trim()) {
    message.warning('请输入提示词')
    return
  }

  if (!loginUserStore.isLogin) {
    message.warning('请先登录')
    router.push('/user/login')
    return
  }

  createLoading.value = true
  try {
    const res = await addApp({initPrompt: promptInput.value.trim()})
    if (res.data.code === 0 && res.data.data) {
      message.success('创建成功，正在跳转...')
      const appId = res.data.data
      router.push(`/app/chat/${appId}`)
    } else {
      message.error(res.data.message || '创建失败')
    }
  } catch (error) {
    message.error('创建失败，请稍后重试')
  } finally {
    createLoading.value = false
  }
}

const handleExampleClick = (prompt: string) => {
  promptInput.value = prompt
}
// 点击按钮，把内容填进去
const setInput = (num) => {
  if (num === 1) {
    promptInput.value = examplePrompts[0].prompt
  }
  if (num === 2) {
    promptInput.value = examplePrompts[1].prompt
  }
  if (num === 3) {
    promptInput.value = examplePrompts[2].prompt
  }
  if (num === 4) {
    promptInput.value = examplePrompts[3].prompt
  }
}

const fetchMyApps = async () => {
  myAppsLoading.value = true
  try {
    if (!loginUserStore.isLogin) {
      myApps.value = []
      myAppsTotal.value = 0
      return
    }
    const res = await listMyAppVoByPage({
      pageNum: myAppsCurrentPage.value,
      pageSize: myAppsPageSize.value,
      appName: myAppsSearchName.value || undefined,
    })
    if (res.data.code === 0 && res.data.data) {
      myApps.value = res.data.data.records || []
      myAppsTotal.value = res.data.data.totalRow || 0
    } else {
      message.error(res.data.message || '获取我的应用列表失败')
    }
  } catch (error) {
    message.error('获取我的应用列表失败，请稍后重试')
  } finally {
    myAppsLoading.value = false
  }
}

const handleMyAppsSearch = () => {
  myAppsCurrentPage.value = 1
  fetchMyApps()
}

const handleMyAppsReset = () => {
  myAppsSearchName.value = ''
  myAppsCurrentPage.value = 1
  fetchMyApps()
}

const handleMyAppsPageChange = (page: number) => {
  myAppsCurrentPage.value = page
  fetchMyApps()
}

const fetchGoodApps = async () => {
  goodAppsLoading.value = true
  try {
    const res = await listGoodAppVoByPage({
      pageNum: goodAppsCurrentPage.value,
      pageSize: goodAppsPageSize.value,
      appName: goodAppsSearchName.value || undefined,
    })
    if (res.data.code === 0 && res.data.data) {
      goodApps.value = res.data.data.records || []
      goodAppsTotal.value = res.data.data.totalRow || 0
    } else {
      message.error(res.data.message || '获取精选应用列表失败')
    }
  } catch (error) {
    message.error('获取精选应用列表失败，请稍后重试')
  } finally {
    goodAppsLoading.value = false
  }
}

const handleGoodAppsSearch = () => {
  goodAppsCurrentPage.value = 1
  fetchGoodApps()
}

const handleGoodAppsReset = () => {
  goodAppsSearchName.value = ''
  goodAppsCurrentPage.value = 1
  fetchGoodApps()
}

const handleGoodAppsPageChange = (page: number) => {
  goodAppsCurrentPage.value = page
  fetchGoodApps()
}

const goToAppChat = (appId: string) => {
  router.push(`/app/chat/${appId}`)
}

onMounted(() => {
  nextTick(() => {
    typePlaceholderStart()
  })
  fetchMyApps()
  fetchGoodApps()
})
</script>

<template>
  <div class="home-container">
    <div class="hero-section">
      <div class="hero-content">
        <h1 class="hero-title">
          与 AI 对话
          <div class="hero-badge">
            <ThunderboltOutlined />
          </div>
          <br />
          <span class="highlight-text">轻松创建应用和网站</span>
        </h1>
        <p class="hero-subtitle">使用自然语言描述你的想法，AI 即可为你生成完整的网站应用</p>
      </div>
      <div class="chat-actions">
        <div class="prompt-input-wrapper">
          <a-textarea
            v-model:value="promptInput"
            :placeholder="typePlaceholder"
            :auto-size="{ minRows: 3, maxRows: 8 }"
            class="prompt-textarea"
          />
          <div class="prompt-actions">
            <a-button
              type="primary"
              size="large"
              :loading="createLoading"
              @click="handleCreateApp"
              class="create-button"
            >
              <template #icon>
                <ArrowUpOutlined />
              </template>
            </a-button>
          </div>
        </div>
      </div>
      <div class="prompt-examples">
        <div class="card-item" @click="setInput(1)">波普风电商页面</div>
        <div class="card-item" @click="setInput(2)">企业网站</div>
        <div class="card-item" @click="setInput(3)">电商运营后台</div>
        <div class="card-item" @click="setInput(4)">暗黑话题社区</div>
      </div>
    </div>

    <!--    <div class="prompt-section">-->
    <!--      <div class="prompt-card">-->
    <!--        <div class="examples-section">-->
    <!--          <div class="examples-header">-->
    <!--            <span>试试这些示例</span>-->
    <!--          </div>-->
    <!--          <div class="examples-grid">-->
    <!--            <div-->
    <!--              v-for="(example, index) in examplePrompts"-->
    <!--              :key="index"-->
    <!--              class="example-card"-->
    <!--              @click="handleExampleClick(example.prompt)"-->
    <!--            >-->
    <!--              <div class="example-icon">-->
    <!--                <component :is="example.icon" />-->
    <!--              </div>-->
    <!--              <div class="example-content">-->
    <!--                <div class="example-title">{{ example.title }}</div>-->
    <!--                <div class="example-desc">{{ example.desc }}</div>-->
    <!--              </div>-->
    <!--              <RightOutlined class="example-arrow" />-->
    <!--            </div>-->
    <!--          </div>-->
    <!--        </div>-->
    <!--      </div>-->
    <!--    </div>-->

    <div class="good-apps-section">
      <div class="section-header">
        <div class="section-title">
          <h2>案例广场</h2>
          <p>探索社区创作的精彩应用，获取灵感</p>
        </div>
        <div class="section-actions">
          <a-input
            v-model:value="goodAppsSearchName"
            placeholder="搜索案例"
            allow-clear
            class="search-input"
            @press-enter="handleGoodAppsSearch"
          >
            <template #prefix>
              <SearchOutlined />
            </template>
          </a-input>
          <a-button @click="handleGoodAppsReset" class="icon-button">
            <ReloadOutlined />
          </a-button>
        </div>
      </div>

      <div class="apps-grid">
        <a-spin :spinning="goodAppsLoading">
          <div v-if="goodAppsLoading" class="skeleton-grid">
            <div v-for="i in 8" :key="i" class="app-skeleton">
              <a-skeleton active :loading="true">
                <a-skeleton-image class="skeleton-cover" />
                <a-skeleton-input style="width: 100%; margin-top: 12px" />
                <a-skeleton-input style="width: 100%; margin-top: 8px" size="small" />
              </a-skeleton>
            </div>
          </div>
          <div v-else-if="goodApps.length === 0" class="empty-state">
            <a-empty description="暂无案例" />
          </div>
          <div v-else class="apps-list">
            <div
              v-for="app in goodApps"
              :key="app.id"
              class="app-card featured"
              @click="goToAppChat(app.id!)"
            >
              <div class="app-cover">
                <img v-if="app.cover" :src="app.cover" :alt="app.appName" />
                <div v-else class="default-cover">
                  <span>{{ app.appName?.charAt(0) || 'A' }}</span>
                </div>
                <div class="featured-badge">
                  <StarOutlined />
                  <span>精选</span>
                </div>
              </div>
              <div class="app-info">
                <h3 class="app-name">{{ app.appName || '未命名应用' }}</h3>
                <p class="app-desc">{{ app.initPrompt || '暂无描述' }}</p>
              </div>
            </div>
          </div>
        </a-spin>
      </div>

      <div v-if="goodAppsTotal > 0" class="pagination">
        <a-pagination
          v-model:current="goodAppsCurrentPage"
          v-model:page-size="goodAppsPageSize"
          :total="goodAppsTotal"
          :show-size-changer="false"
          :show-quick-jumper="true"
          :show-total="(total: number) => `共 ${total} 条`"
          @change="handleGoodAppsPageChange"
        />
      </div>
    </div>

    <div class="my-apps-section">
      <div class="section-header">
        <div class="section-title">
          <h2>我的应用</h2>
          <p>管理你创建的所有应用</p>
        </div>
        <div class="section-actions">
          <a-input
            v-model:value="myAppsSearchName"
            placeholder="搜索应用"
            allow-clear
            class="search-input"
            @press-enter="handleMyAppsSearch"
          >
            <template #prefix>
              <SearchOutlined />
            </template>
          </a-input>
          <a-button @click="handleMyAppsReset" class="icon-button">
            <ReloadOutlined />
          </a-button>
        </div>
      </div>

      <div class="apps-grid">
        <div v-if="loginUserStore.isLogin">
          <a-spin :spinning="myAppsLoading">
            <div v-if="myAppsLoading" class="skeleton-grid">
              <div v-for="i in 8" :key="i" class="app-skeleton">
                <a-skeleton active :loading="true">
                  <a-skeleton-image class="skeleton-cover" />
                  <a-skeleton-input style="width: 100%; margin-top: 12px" />
                  <a-skeleton-input style="width: 100%; margin-top: 8px" size="small" />
                </a-skeleton>
              </div>
            </div>
            <div v-else-if="myApps.length === 0" class="empty-state">
              <a-empty description="暂无应用，快来创建一个吧！">
                <a-button type="primary" @click="promptInput = examplePrompts[0]?.prompt ?? ''">
                  从示例开始
                </a-button>
              </a-empty>
            </div>
            <div v-else class="apps-list">
              <div
                v-for="app in myApps"
                :key="app.id"
                class="app-card"
                @click="goToAppChat(app.id!)"
              >
                <div class="app-cover">
                  <img v-if="app.cover" :src="app.cover" :alt="app.appName" />
                  <div v-else class="default-cover">
                    <span>{{ app.appName?.charAt(0) || 'A' }}</span>
                  </div>
                </div>
                <div class="app-info">
                  <h3 class="app-name">{{ app.appName || '未命名应用' }}</h3>
                  <p class="app-desc">{{ app.initPrompt || '暂无描述' }}</p>
                </div>
              </div>
            </div>
          </a-spin>
        </div>
        <div v-else class="login-tip">
          <a-empty description="请先登录查看您的应用">
            <a-button type="primary" @click="router.push('/user/login')"> 去登录</a-button>
          </a-empty>
        </div>
      </div>

      <div v-if="myAppsTotal > 0" class="pagination">
        <a-pagination
          v-model:current="myAppsCurrentPage"
          v-model:page-size="myAppsPageSize"
          :total="myAppsTotal"
          :show-size-changer="false"
          :show-quick-jumper="true"
          :show-total="(total: number) => `共 ${total} 条`"
          @change="handleMyAppsPageChange"
        />
      </div>
    </div>
  </div>
</template>

<style scoped>
.prompt-examples {
  display: flex;
  justify-content: center;
  margin-top: 10px;
}

.card-item {
  display: flex;
  width: 120px;
  height: 30px;
  background-color: #ffffff;
  border: 2px solid #ffffff;
  border-radius: 6px;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.3s;
  margin-right: 12px;
}

.card-item:last-child {
  margin-right: 0;
}

.card-item:hover {
  background-color: #adedbb;
  color: #fff;
}

.home-container {
  max-width: 1280px;
  margin: 0 auto;
  padding: 24px 24px 40px;
  min-height: calc(100vh - 56px);
}

.hero-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  margin: 60px 0;
  background: transparent;
  border-radius: 8px;
  border: none;
}

.chat-actions {
  border: 1px solid #ddd; /* 浅灰色边框，和你原有设计统一 */
  border-radius: 20px; /* 和输入框圆角一致，无棱角 */
  width: 100%;
  max-width: 720px;
  display: flex;
  background: white;
  justify-content: center;
  align-items: center;
  margin: 0 auto;
}

.hero-badge {
  display: inline-flex;
  align-items: center; /* 垂直居中（核心） */
  justify-content: center;
  gap: 8px;
  padding: 6px 16px; /* 调小一点，更贴近文字高度 */
  background: white;
  border-radius: 4px;
  font-size: 14px;
  font-weight: 500;
  color: #1890ff;

  /* 👇 下面这 3 行是对齐关键 */
  vertical-align: middle; /* 和旁边文字垂直对齐 */
  line-height: 1; /* 不撑高行高 */
  margin-bottom: 32px;
}

.hero-title {
  font-size: 48px;
  font-weight: 700;
  line-height: 1.2;
}

.highlight-text {
  background: linear-gradient(90deg, #6366f1, #06b6d4);
  -webkit-background-clip: text;
  color: transparent;
}

.hero-subtitle {
  font-size: 14px;
  margin-bottom: 40px;
  color: #4b5563;
  max-width: 480px;
  line-height: 1.7;
  font-family:
    -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

.prompt-section {
  margin-bottom: 32px;
}

.prompt-card {
  border: none;
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.08);
  padding: 24px;
}

.prompt-input-wrapper {
  width: 100%;
  max-width: 720px;
}

.prompt-textarea {
  padding: 8px 10px;
  border-radius: 20px;
  font-size: 16px;
  border: none;
}

.prompt-textarea:focus {
  box-shadow: none;
}

.prompt-actions {
  border-radius: 20px;
  height: 35px;
  display: flex;
  background: white;
  justify-content: flex-end;
}

.create-button {
  /* 强制宽高一样 */
  width: 30px !important;
  height: 30px !important;
  padding: 5px !important;
  border-radius: 50% !important;
  margin: 2.5px !important;
  display: flex !important;
  justify-content: center !important;
  align-items: center !important;
}

.create-button:hover {
  background: #40a9ff;
  border-color: #40a9ff;
}

.examples-section {
  border-top: 1px solid #e5e7eb;
  padding-top: 24px;
}

.examples-header {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin-bottom: 24px;
  font-family:
    -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

.examples-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.example-card {
  border: none;
  border-radius: 16px;
  background: white;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.05);
  transition: all 0.3s;
}

.example-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
}

.example-icon {
  font-size: 24px;
  color: #4b5563;
  flex-shrink: 0;
}

.example-content {
  flex: 1;
}

.example-title {
  font-size: 14px;
  font-weight: 600;
  color: #111827;
  margin-bottom: 6px;
  font-family:
    -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

.example-desc {
  font-size: 13px;
  color: #6b7280;
  line-height: 1.6;
  font-family:
    -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

.example-arrow {
  color: #9ca3af;
  font-size: 14px;
}

.good-apps-section,
.my-apps-section {
  margin-bottom: 40px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.section-title h2 {
  font-size: 18px;
  font-weight: 600;
  margin: 0 0 8px 0;
  color: #111827;
  font-family:
    -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

.section-title p {
  font-size: 14px;
  color: #6b7280;
  margin: 0;
  font-family:
    -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

.section-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.search-input {
  width: 280px;
}

.icon-button {
  border-radius: 4px;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.apps-grid {
  min-height: 200px;
}

.skeleton-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 24px;
}

.app-skeleton {
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 4px;
  padding: 16px;
}

.skeleton-cover {
  width: 100%;
  height: 180px;
  border-radius: 6px;
}

.apps-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 24px;
}

.app-card {
  border: none;
  border-radius: 16px;
  overflow: hidden;
  background: white;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.05);
  transition: all 0.3s;
}

.app-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.1);
}

.app-card.featured {
  border: 1px solid #f97316;
}

.app-cover {
  width: 100%;
  height: 180px;
  overflow: hidden;
  background: #f3f4f6;
  position: relative;
}

.app-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.default-cover {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #111827;
  color: white;
  font-size: 56px;
  font-weight: 700;
}

.featured-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  background: #f97316;
  color: white;
  padding: 6px 12px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 4px;
}

.app-info {
  padding: 20px;
}

.app-name {
  font-size: 16px;
  font-weight: 600;
  color: #111827;
  margin: 0 0 8px 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  font-family:
    -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

.app-desc {
  font-size: 13px;
  color: #6b7280;
  line-height: 1.6;
  margin: 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  height: 42px;
  font-family:
    -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

.empty-state,
.login-tip {
  padding: 60px 0;
  text-align: center;
}

.pagination {
  margin-top: 24px;
  text-align: center;
}

@media (max-width: 768px) {
  .home-container {
    padding: 16px;
  }

  .hero-section {
    grid-template-columns: minmax(0, 1fr);
    padding: 20px 16px;
    margin-bottom: 24px;
  }

  .prompt-card {
    padding: 16px;
  }

  .examples-grid {
    grid-template-columns: 1fr;
  }

  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 20px;
  }

  .section-actions {
    width: 100%;
  }

  .section-actions .search-input {
    flex: 1;
    width: 100%;
  }

  .apps-list {
    grid-template-columns: 1fr;
  }
}
</style>
