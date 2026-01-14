<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useLoginUserStore } from '@/stores/LoginUser'

const router = useRouter()
const loginUserStore = useLoginUserStore()

const loginUser = computed(() => loginUserStore.loginUser)

const formatTime = (time?: string) => {
  if (!time) return '未知'
  return new Date(time).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const handleEditProfile = () => {
  router.push('/user/profile/edit')
}

const handleChangePassword = () => {
  router.push('/user/password')
}

const getRoleTagColor = (role?: string) => {
  const roleColorMap: Record<string, string> = {
    admin: 'red',
    user: 'blue',
    vip: 'gold',
    default: 'default'
  }
  return roleColorMap[role || ''] || roleColorMap.default
}

const getRoleText = (role?: string) => {
  const roleTextMap: Record<string, string> = {
    admin: '管理员',
    user: '普通用户',
    vip: 'VIP用户',
    default: '未知'
  }
  return roleTextMap[role || ''] || roleTextMap.default
}
</script>

<template>
  <div class="profile-container">
    <a-page-header
      title="个人中心"
      sub-title="查看和管理您的个人信息"
      class="page-header"
    />

    <div class="profile-content">
      <a-row :gutter="[24, 24]">
        <a-col :xs="24" :sm="24" :md="8" :lg="6" :xl="6">
          <a-card class="profile-card profile-card-left" :bordered="false">
            <div class="profile-avatar-section">
              <a-avatar
                :size="120"
                :src="loginUser.userAvatar"
                class="profile-avatar"
              >
                {{ loginUser.userName?.charAt(0) || 'U' }}
              </a-avatar>
              <h3 class="profile-name">{{ loginUser.userName || '未设置' }}</h3>
              <a-tag :color="getRoleTagColor(loginUser.userRole)" class="profile-role">
                {{ getRoleText(loginUser.userRole) }}
              </a-tag>
            </div>

            <a-divider />

            <div class="profile-stats">
              <div class="stat-item">
                <div class="stat-label">用户ID</div>
                <div class="stat-value">{{ loginUser.id || '-' }}</div>
              </div>
            </div>
          </a-card>
        </a-col>

        <a-col :xs="24" :sm="24" :md="16" :lg="18" :xl="18">
          <a-card title="基本信息" class="profile-card" :bordered="false">
            <template #extra>
              <a-space>
                <a-button type="primary" @click="handleEditProfile">
                  编辑资料
                </a-button>
                <a-button @click="handleChangePassword">
                  修改密码
                </a-button>
              </a-space>
            </template>

            <a-descriptions :column="{ xs: 1, sm: 1, md: 2, lg: 2, xl: 2 }" bordered>
              <a-descriptions-item label="用户名">
                {{ loginUser.userName || '未设置' }}
              </a-descriptions-item>

              <a-descriptions-item label="用户ID">
                {{ loginUser.id || '-' }}
              </a-descriptions-item>

              <a-descriptions-item label="用户角色">
                <a-tag :color="getRoleTagColor(loginUser.userRole)">
                  {{ getRoleText(loginUser.userRole) }}
                </a-tag>
              </a-descriptions-item>

              <a-descriptions-item label="手机号">
                {{ loginUser.phone || '未绑定' }}
              </a-descriptions-item>

              <a-descriptions-item label="邮箱">
                {{ loginUser.email || '未绑定' }}
              </a-descriptions-item>

              <a-descriptions-item label="注册时间">
                {{ formatTime(loginUser.createTime) }}
              </a-descriptions-item>
            </a-descriptions>
          </a-card>

          <a-card title="账户安全" class="profile-card security-card" :bordered="false">
            <a-list :data-source="[
              { label: '登录密码', status: loginUser.userPassword ? '已设置' : '未设置', icon: '🔒' },
              { label: '手机号', status: loginUser.phone ? '已绑定' : '未绑定', icon: '📱' },
              { label: '邮箱', status: loginUser.email ? '已绑定' : '未绑定', icon: '📧' }
            ]">
              <template #renderItem="{ item }">
                <a-list-item>
                  <a-list-item-meta>
                    <template #avatar>
                      <span class="security-icon">{{ item.icon }}</span>
                    </template>
                    <template #title>{{ item.label }}</template>
                    <template #description>
                      <a-tag :color="item.status === '已设置' || item.status === '已绑定' ? 'success' : 'default'">
                        {{ item.status }}
                      </a-tag>
                    </template>
                  </a-list-item-meta>
                </a-list-item>
              </template>
            </a-list>
          </a-card>
        </a-col>
      </a-row>
    </div>
  </div>
</template>

<style scoped>
.profile-container {
  padding: 24px;
  background-color: #f0f2f5;
  min-height: calc(100vh - 64px);
}

.page-header {
  background-color: #fff;
  padding: 16px 24px;
  margin-bottom: 24px;
  border-radius: 8px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
}

.profile-content {
  max-width: 1400px;
  margin: 0 auto;
}

.profile-card {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
  transition: all 0.3s;
}

.profile-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.profile-card-left {
  position: sticky;
  top: 24px;
}

.profile-avatar-section {
  text-align: center;
  padding: 16px 0;
}

.profile-avatar {
  margin-bottom: 16px;
  border: 3px solid #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  transition: all 0.3s;
}

.profile-avatar:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.2);
}

.profile-name {
  font-size: 20px;
  font-weight: 600;
  color: #262626;
  margin-bottom: 12px;
}

.profile-role {
  font-size: 14px;
}

.profile-stats {
  padding: 8px 0;
}

.stat-item {
  text-align: center;
  padding: 12px 0;
}

.stat-label {
  font-size: 14px;
  color: #8c8c8c;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 18px;
  font-weight: 600;
  color: #262626;
}

.security-card {
  margin-top: 24px;
}

.security-icon {
  font-size: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  background-color: #f5f5f5;
  border-radius: 50%;
}

:deep(.ant-descriptions-item-label) {
  font-weight: 500;
  background-color: #fafafa;
}

:deep(.ant-descriptions-item-content) {
  color: #595959;
}

:deep(.ant-list-item) {
  padding: 16px 24px;
}

:deep(.ant-list-item-meta-title) {
  font-size: 15px;
  font-weight: 500;
}

@media (max-width: 768px) {
  .profile-container {
    padding: 16px;
  }

  .profile-card-left {
    position: static;
  }
}
</style>
