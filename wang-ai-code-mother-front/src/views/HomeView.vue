<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { PlusOutlined, SearchOutlined, ReloadOutlined, ThunderboltOutlined, BulbOutlined, ShoppingOutlined, FileTextOutlined, BankOutlined, BarChartOutlined, StarOutlined } from '@ant-design/icons-vue'
import { addApp, listMyAppVoByPage, listGoodAppVoByPage } from '@/api/appController'
import { useLoginUserStore } from '@/stores/LoginUser'

const router = useRouter()
const loginUserStore = useLoginUserStore()

const promptInput = ref('')
const createLoading = ref(false)

const examplePrompts = [
  {
    icon: ShoppingOutlined,
    title: '电商网站',
    desc: '创建一个现代化的电商网站，包含商品展示、购物车、订单管理等功能',
    prompt: '创建一个现代化的电商网站，包含商品展示、购物车、订单管理等功能'
  },
  {
    icon: FileTextOutlined,
    title: '个人博客',
    desc: '设计一个简洁优雅的个人博客，支持文章发布、评论、标签分类',
    prompt: '设计一个简洁优雅的个人博客，支持文章发布、评论、标签分类'
  },
  {
    icon: BankOutlined,
    title: '企业官网',
    desc: '搭建一个专业的企业官网，包含公司介绍、产品展示、联系我们',
    prompt: '搭建一个专业的企业官网，包含公司介绍、产品展示、联系我们'
  },
  {
    icon: BarChartOutlined,
    title: '数据仪表盘',
    desc: '开发一个数据可视化仪表盘，展示关键指标和图表分析',
    prompt: '开发一个数据可视化仪表盘，展示关键指标和图表分析'
  }
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
    const res = await addApp({ initPrompt: promptInput.value.trim() })
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
      appName: myAppsSearchName.value || undefined
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
      appName: goodAppsSearchName.value || undefined
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
  fetchMyApps()
  fetchGoodApps()
})
</script>

<template>
  <div class="home-container">
    <div class="hero-section">
      <div class="hero-content">
        <div class="hero-badge">
          <ThunderboltOutlined />
          <span>AI 驱动，即时生成</span>
        </div>
        <h1 class="hero-title">
          用自然语言<br />
          <span class="gradient-text">创造你的应用</span>
        </h1>
        <p class="hero-subtitle">
          无需编程知识，只需描述你的想法，AI 即可为你生成完整的网站应用
        </p>
      </div>
    </div>

    <div class="prompt-section">
      <a-card :bordered="false" class="prompt-card">
        <div class="prompt-input-wrapper">
          <a-textarea
            v-model:value="promptInput"
            placeholder="描述你想要创建的应用，例如：创建一个现代化的电商网站，包含商品展示、购物车功能..."
            :auto-size="{ minRows: 3, maxRows: 6 }"
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
                <PlusOutlined />
              </template>
              立即创建
            </a-button>
          </div>
        </div>

        <div class="examples-section">
          <div class="examples-header">
            <BulbOutlined />
            <span>试试这些示例</span>
          </div>
          <div class="examples-grid">
            <div
              v-for="(example, index) in examplePrompts"
              :key="index"
              class="example-card"
              @click="handleExampleClick(example.prompt)"
            >
              <div class="example-icon">
                <component :is="example.icon" />
              </div>
              <div class="example-content">
                <div class="example-title">{{ example.title }}</div>
                <div class="example-desc">{{ example.desc }}</div>
              </div>
            </div>
          </div>
        </div>
      </a-card>
    </div>

    <div class="my-apps-section">
      <a-card :bordered="false" class="apps-card">
        <template #title>
          <div class="section-title">
            <span>我的应用</span>
            <a-tag v-if="myAppsTotal > 0" color="blue">{{ myAppsTotal }}</a-tag>
          </div>
        </template>

        <template #extra>
          <a-space>
            <a-input
              v-model:value="myAppsSearchName"
              placeholder="搜索应用名称"
              allow-clear
              style="width: 200px"
              @press-enter="handleMyAppsSearch"
            >
              <template #prefix>
                <SearchOutlined />
              </template>
            </a-input>
            <a-button @click="handleMyAppsSearch">
              <template #icon>
                <SearchOutlined />
              </template>
            </a-button>
            <a-button @click="handleMyAppsReset">
              <template #icon>
                <ReloadOutlined />
              </template>
            </a-button>
          </a-space>
        </template>

        <div v-if="loginUserStore.isLogin" class="apps-list">
          <a-spin :spinning="myAppsLoading">
            <div v-if="myAppsLoading" class="skeleton-grid">
              <a-row :gutter="[16, 16]">
                <a-col v-for="i in 8" :key="i" :xs="24" :sm="12" :md="8" :lg="6">
                  <a-card>
                    <a-skeleton active :loading="true">
                      <a-skeleton-image />
                      <a-skeleton-input style="width: 100%; margin-top: 12px;" />
                      <a-skeleton-input style="width: 100%; margin-top: 8px;" size="small" />
                    </a-skeleton>
                  </a-card>
                </a-col>
              </a-row>
            </div>
            <div v-else-if="myApps.length === 0" class="empty-state">
              <a-empty description="暂无应用，快来创建一个吧！">
                <a-button type="primary" @click="promptInput = examplePrompts[0].prompt">
                  从示例开始
                </a-button>
              </a-empty>
            </div>
            <a-row v-else :gutter="[16, 16]">
              <a-col
                v-for="app in myApps"
                :key="app.id"
                :xs="24"
                :sm="12"
                :md="8"
                :lg="6"
              >
                <a-card
                  hoverable
                  class="app-card"
                  @click="goToAppChat(app.id!)"
                >
                  <template #cover>
                    <div class="app-cover">
                      <img
                        v-if="app.cover"
                        :src="app.cover"
                        :alt="app.appName"
                      />
                      <div v-else class="default-cover">
                        <span>{{ app.appName?.charAt(0) || 'A' }}</span>
                      </div>
                    </div>
                  </template>
                  <a-card-meta :title="app.appName || '未命名应用'">
                    <template #description>
                      <div class="app-desc">
                        {{ app.initPrompt || '暂无描述' }}
                      </div>
                    </template>
                  </a-card-meta>
                </a-card>
              </a-col>
            </a-row>
          </a-spin>
        </div>
        <div v-else class="login-tip">
          <a-empty description="请先登录查看您的应用">
            <a-button type="primary" @click="router.push('/user/login')">
              去登录
            </a-button>
          </a-empty>
        </div>

        <div v-if="myAppsTotal > 0" class="pagination">
          <a-pagination
            v-model:current="myAppsCurrentPage"
            v-model:page-size="myAppsPageSize"
            :total="myAppsTotal"
            :show-size-changer="false"
            :show-quick-jumper="true"
            :show-total="(total) => `共 ${total} 条`"
            @change="handleMyAppsPageChange"
          />
        </div>
      </a-card>
    </div>

    <div class="good-apps-section">
      <a-card :bordered="false" class="apps-card">
        <template #title>
          <div class="section-title">
            <span>精选应用</span>
            <a-tag v-if="goodAppsTotal > 0" color="gold">{{ goodAppsTotal }}</a-tag>
          </div>
        </template>

        <template #extra>
          <a-space>
            <a-input
              v-model:value="goodAppsSearchName"
              placeholder="搜索应用名称"
              allow-clear
              style="width: 200px"
              @press-enter="handleGoodAppsSearch"
            >
              <template #prefix>
                <SearchOutlined />
              </template>
            </a-input>
            <a-button @click="handleGoodAppsSearch">
              <template #icon>
                <SearchOutlined />
              </template>
            </a-button>
            <a-button @click="handleGoodAppsReset">
              <template #icon>
                <ReloadOutlined />
              </template>
            </a-button>
          </a-space>
        </template>

        <div class="apps-list">
          <a-spin :spinning="goodAppsLoading">
            <div v-if="goodAppsLoading" class="skeleton-grid">
              <a-row :gutter="[16, 16]">
                <a-col v-for="i in 8" :key="i" :xs="24" :sm="12" :md="8" :lg="6">
                  <a-card>
                    <a-skeleton active :loading="true">
                      <a-skeleton-image />
                      <a-skeleton-input style="width: 100%; margin-top: 12px;" />
                      <a-skeleton-input style="width: 100%; margin-top: 8px;" size="small" />
                    </a-skeleton>
                  </a-card>
                </a-col>
              </a-row>
            </div>
            <div v-else-if="goodApps.length === 0" class="empty-state">
              <a-empty description="暂无精选应用" />
            </div>
            <a-row v-else :gutter="[16, 16]">
              <a-col
                v-for="app in goodApps"
                :key="app.id"
                :xs="24"
                :sm="12"
                :md="8"
                :lg="6"
              >
                <a-card
                  hoverable
                  class="app-card featured"
                  @click="goToAppChat(app.id!)"
                >
                  <template #cover>
                    <div class="app-cover">
                      <img
                        v-if="app.cover"
                        :src="app.cover"
                        :alt="app.appName"
                      />
                      <div v-else class="default-cover">
                        <span>{{ app.appName?.charAt(0) || 'A' }}</span>
                      </div>
                      <div class="featured-badge">
                        <StarOutlined />
                        <span>精选</span>
                      </div>
                    </div>
                  </template>
                  <a-card-meta :title="app.appName || '未命名应用'">
                    <template #description>
                      <div class="app-desc">
                        {{ app.initPrompt || '暂无描述' }}
                      </div>
                    </template>
                  </a-card-meta>
                </a-card>
              </a-col>
            </a-row>
          </a-spin>
        </div>

        <div v-if="goodAppsTotal > 0" class="pagination">
          <a-pagination
            v-model:current="goodAppsCurrentPage"
            v-model:page-size="goodAppsPageSize"
            :total="goodAppsTotal"
            :show-size-changer="false"
            :show-quick-jumper="true"
            :show-total="(total) => `共 ${total} 条`"
            @change="handleGoodAppsPageChange"
          />
        </div>
      </a-card>
    </div>
  </div>
</template>

<style scoped>
.home-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

.hero-section {
  text-align: center;
  margin-bottom: 48px;
  padding: 60px 40px;
  background: #1890ff;
  border-radius: 24px;
  color: white;
  position: relative;
  overflow: hidden;
  animation: fadeInUp 0.8s ease-out;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.hero-section::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.1) 0%, transparent 60%);
  animation: rotate 20s linear infinite;
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.hero-content {
  position: relative;
  z-index: 1;
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 20px;
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 24px;
  backdrop-filter: blur(10px);
}

.hero-title {
  font-size: 56px;
  font-weight: 800;
  margin: 0 0 20px 0;
  line-height: 1.2;
  color: white;
}

.gradient-text {
  background: #fff;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.hero-subtitle {
  font-size: 18px;
  margin: 0;
  opacity: 0.95;
  max-width: 600px;
  margin-left: auto;
  margin-right: auto;
  line-height: 1.6;
}

.prompt-section {
  margin-bottom: 48px;
  animation: fadeInUp 0.8s ease-out 0.2s both;
}

.prompt-card {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  border-radius: 16px;
}

.prompt-input-wrapper {
  margin-bottom: 32px;
}

.prompt-textarea {
  font-size: 16px;
  border-radius: 12px;
  border: 2px solid #e8e8e8;
  transition: all 0.3s;
}

.prompt-textarea:focus {
  border-color: #667eea;
  box-shadow: 0 0 0 4px rgba(102, 126, 234, 0.1);
}

.prompt-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.create-button {
  height: 48px;
  padding: 0 32px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 12px;
  background: #1890ff;
  border: none;
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.4);
  transition: all 0.3s;
}

.create-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(24, 144, 255, 0.5);
}

.examples-section {
  border-top: 1px solid #f0f0f0;
  padding-top: 24px;
}

.examples-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 16px;
}

.examples-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 16px;
}

.example-card {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 16px;
  background: #fafafa;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
  border: 2px solid transparent;
}

.example-card:hover {
  background: white;
  border-color: #667eea;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.example-icon {
  font-size: 32px;
  flex-shrink: 0;
}

.example-content {
  flex: 1;
}

.example-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
}

.example-desc {
  font-size: 12px;
  color: #666;
  line-height: 1.5;
}

.my-apps-section,
.good-apps-section {
  margin-bottom: 48px;
  animation: fadeInUp 0.8s ease-out 0.4s both;
}

.apps-card {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  border-radius: 16px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 20px;
  font-weight: 600;
  color: #333;
}

.apps-list {
  min-height: 200px;
}

.app-card {
  cursor: pointer;
  transition: all 0.3s;
  border-radius: 12px;
  overflow: hidden;
  animation: fadeIn 0.6s ease-out both;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.app-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.15);
}

.app-card.featured {
  border: 2px solid #ffd700;
}

.app-cover {
  width: 100%;
  height: 180px;
  overflow: hidden;
  background: #f5f5f5;
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
  background: #1890ff;
  color: white;
  font-size: 56px;
  font-weight: 700;
}

.featured-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  background: #ff6b6b;
  color: white;
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(255, 107, 107, 0.4);
}

.app-desc {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  height: 40px;
  line-height: 20px;
}

.empty-state,
.login-tip {
  padding: 60px 0;
}

.pagination {
  margin-top: 24px;
  text-align: center;
}

:deep(.ant-card-head-title) {
  padding: 20px 0;
}

:deep(.ant-card-meta-title) {
  font-size: 16px;
  font-weight: 600;
}

:deep(.ant-card-meta-description) {
  color: #666;
}

:deep(.ant-input) {
  border-radius: 8px;
}

:deep(.ant-btn) {
  border-radius: 8px;
}

@media (max-width: 768px) {
  .hero-title {
    font-size: 36px;
  }

  .hero-subtitle {
    font-size: 16px;
  }

  .examples-grid {
    grid-template-columns: 1fr;
  }

  .app-card {
    margin-bottom: 16px;
  }
}
</style>
