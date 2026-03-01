<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import request from '@/request'

type Article = {
  id: string | number
  title: string
  author?: string
  sourceName?: string
  originalUrl: string
  publishTime?: string
  summary?: string
  category?: string
  isFeatured?: boolean
  isTop?: boolean
  hotScore?: number
  cover?: string
}

type ArticleRecord = {
  id?: string | number
  articleId?: string | number
  title?: string
  author?: string
  sourceName?: string
  sourcePlatform?: string
  sourceUrl?: string
  originalUrl?: string
  publishTime?: string
  summary?: string
  category?: string
  isFeatured?: string | number | boolean
  isTop?: string | number | boolean
  hotScore?: string | number
  cover?: string
}

const categories = [
  { key: 'all', label: '全部' },
  { key: 'ai_tech', label: 'AI 技术' },
  { key: 'industry', label: '行业动态' },
  { key: 'tools', label: '工具教程' },
]

const activeCategory = ref('all')
const keyword = ref('')
const sortBy = ref<'time' | 'hot'>('time')
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const loading = ref(false)
const articles = ref<Article[]>([])
const triggerLoading = ref(false)

const paramsComputed = computed(() => ({
  category: activeCategory.value === 'all' ? undefined : activeCategory.value,
  keyword: keyword.value || undefined,
  sort: sortBy.value,
  current: page.value,
  pageSize: pageSize.value,
}))

const getMockArticles = () => {
  const now = new Date()
  const data: Article[] = Array.from({ length: pageSize.value }, (_, i) => {
    const idx = (page.value - 1) * pageSize.value + i + 1
    const categoryIndex = (idx % (categories.length - 1)) + 1
    const cat = categories[categoryIndex]?.label || categories[0]?.label || 'AI 技术'
    return {
      id: idx,
      title: `示例文章标题 ${idx}`,
      author: `作者${idx}`,
      sourceName: '机器之心',
      originalUrl: 'https://www.jiqizhixin.com/',
      publishTime: new Date(now.getTime() - idx * 3600_000).toISOString(),
      summary: '这是一段示例摘要，用于展示文章的简要内容与关键信息。',
      category: cat,
      isFeatured: idx % 7 === 0,
      isTop: idx % 13 === 0,
      hotScore: Math.floor(Math.random() * 1000),
    }
  })
  return { records: data, total: 80 }
}

const fetchArticles = async () => {
  loading.value = true
  try {
    const res = await request.get('/articles/list', {
      params: paramsComputed.value,
    })
    if (res?.data?.data) {
      const records = (res.data.data.records || []) as ArticleRecord[]
      const t = Number(res.data.data.total || 0)
      articles.value = records.map((item) => {
        const rawUrl = String(item.originalUrl || item.sourceUrl || '').trim()
        const cleanedUrl = rawUrl.replace(/`/g, '').trim()
        return {
          id: item.id || item.articleId,
          title: item.title || '',
          author: item.author || '',
          sourceName: item.sourceName || item.sourcePlatform || '',
          originalUrl: cleanedUrl,
          publishTime: item.publishTime || '',
          summary: item.summary || '',
          category: item.category || '',
          isFeatured: Number(item.isFeatured) === 1,
          isTop: Number(item.isTop) === 1,
          hotScore: item.hotScore ? Number(item.hotScore) : undefined,
          cover: item.cover || '',
        } as Article
      })
      total.value = t
    } else {
      const mock = getMockArticles()
      articles.value = mock.records
      total.value = mock.total
    }
  } catch {
    const mock = getMockArticles()
    articles.value = mock.records
    total.value = mock.total
  } finally {
    loading.value = false
  }
}

const onSearch = () => {
  page.value = 1
  fetchArticles()
}

const onChangeCategory = (key: string) => {
  activeCategory.value = key
  page.value = 1
  fetchArticles()
}

const onChangeSort = (val: 'time' | 'hot') => {
  sortBy.value = val
  page.value = 1
  fetchArticles()
}

const onPageChange = (p: number, ps?: number) => {
  page.value = p
  if (ps && ps !== pageSize.value) {
    pageSize.value = ps
    page.value = 1
  }
  fetchArticles()
}

const triggerCrawl = async () => {
  triggerLoading.value = true
  try {
    const res = await request.post('/crawl/trigger')
    if (res?.data?.code === 0) {
      message.success('已触发爬取任务')
      fetchArticles()
    } else {
      message.error(res?.data?.message || '触发爬取任务失败')
    }
  } catch {
    message.error('触发爬取任务失败')
  } finally {
    triggerLoading.value = false
  }
}

onMounted(() => {
  fetchArticles()
})
</script>

<template>
  <div class="articles-page">
    <div class="articles-header">

      <div class="filters">
        <a-input-search
          v-model:value="keyword"
          placeholder="搜索标题、摘要、作者"
          enter-button="搜索"
          class="search"
          @search="onSearch"
        />
        <a-select v-model:value="sortBy" class="sort" @change="onChangeSort">
          <a-select-option value="time">最新</a-select-option>
          <a-select-option value="hot">热度</a-select-option>
        </a-select>
        <a-button @click="fetchArticles">刷新</a-button>
        <a-button type="primary" :loading="triggerLoading" @click="triggerCrawl">手动触发爬取</a-button>
      </div>
    </div>

    <a-alert
      type="info"
      show-icon
      message="文章内容来自爬虫抓取"
      description="所有内容均标注来源与作者，保留阅读原文链接；如涉及版权，请通过版权申诉入口联系我们。"
      style="margin-bottom: 12px"
    />

    <a-tabs :activeKey="activeCategory" @change="onChangeCategory">
      <a-tab-pane v-for="c in categories" :key="c.key" :tab="c.label" />
    </a-tabs>

    <template v-if="!loading && articles.length === 0">
      <a-empty description="暂无文章" />
    </template>
    <template v-else>
      <a-list
        :loading="loading"
        item-layout="vertical"
        :data-source="articles"
      >
        <template #renderItem="{ item }">
          <a-list-item class="article-item">
            <a-card :bordered="false" class="article-card">
              <div class="meta">
                <div class="badges">
                  <a-badge v-if="item.isTop" color="gold" text="置顶" />
                  <a-badge v-if="item.isFeatured" color="green" text="精选" />
                </div>
                <div class="title-line">
                  <a :href="item.originalUrl" target="_blank" rel="noopener" class="title-link">
                    {{ item.title }}
                  </a>
                </div>
                <div class="meta-line">
                  <a-tag v-if="item.sourceName" color="blue">{{ item.sourceName }}</a-tag>
                  <a-tag v-if="item.category" color="geekblue">{{ item.category }}</a-tag>
                  <span class="meta-text" v-if="item.author">作者：{{ item.author }}</span>
                  <span class="meta-dot">·</span>
                  <span class="meta-text" v-if="item.publishTime">
                    {{ new Date(item.publishTime).toLocaleString() }}
                  </span>
                  <span class="meta-dot" v-if="item.hotScore !== undefined">·</span>
                  <span class="meta-text" v-if="item.hotScore !== undefined">热度 {{ item.hotScore }}</span>
                </div>
              </div>
              <div class="summary" v-if="item.summary">
                {{ item.summary }}
              </div>
              <div class="actions">
                <a-button type="link">
                  <a :href="item.originalUrl" target="_blank" rel="noopener">阅读原文</a>
                </a-button>
              </div>
            </a-card>
          </a-list-item>
        </template>
      </a-list>
    </template>

    <div class="pager">
      <a-pagination
        :current="page"
        :pageSize="pageSize"
        :total="total"
        showSizeChanger
        @change="onPageChange"
        @showSizeChange="onPageChange"
      />
    </div>
  </div>
</template>

<style scoped>
.articles-page {
  padding: 24px;
}

.articles-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.title {
  font-size: 22px;
  font-weight: 600;
  color: #333;
}

.filters {
  display: flex;
  gap: 12px;
}

.search {
  width: 360px;
}

.sort {
  width: 120px;
}

.article-card {
  width: 100%;
}

.badges {
  display: flex;
  gap: 8px;
  margin-bottom: 6px;
}

.title-line {
  display: flex;
  align-items: center;
  gap: 8px;
}

.title-link {
  font-size: 18px;
  color: #1f1f1f;
}

.title-link:hover {
  color: #1677ff;
}

.meta-line {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  color: #666;
  margin-top: 6px;
}

.meta-text {
  color: #666;
  font-size: 12px;
}

.meta-dot {
  color: #aaa;
  margin: 0 4px;
}

.summary {
  margin-top: 10px;
  color: #444;
  line-height: 1.7;
}

.actions {
  margin-top: 6px;
}

.pager {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}
</style>
