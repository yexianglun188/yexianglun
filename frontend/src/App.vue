<template>
  <el-config-provider :locale="zhCn">
  <el-container class="app">
    <div class="topbar"></div>
    <el-header class="header">
      <div class="header-inner">
        <div class="logo" @click="$router.push('/')">
          <el-icon :size="22"><School /></el-icon>
          <span>校园二手交易平台</span>
        </div>

        <div class="search">
          <el-input
            v-model="keyword"
            placeholder="搜索想买的二手好物，比如「教材」「耳机」"
            clearable
            @keyup.enter="doSearch"
          >
            <template #append>
              <el-button :icon="Search" @click="doSearch" />
            </template>
          </el-input>
        </div>

        <div class="actions">
          <el-button type="primary" :icon="Plus" @click="goPublish">发布闲置</el-button>

          <template v-if="userStore.isLogin">
            <el-badge :value="unreadCount" :hidden="!unreadCount" :max="99" type="danger">
              <el-button :icon="ChatDotRound" @click="goChat">消息</el-button>
            </el-badge>
            <el-dropdown @command="onCommand">
              <span class="user">
                <el-avatar :size="30" :src="userStore.user?.avatar">
                  {{ userStore.nickname.charAt(0) }}
                </el-avatar>
                <span class="name">{{ userStore.nickname }}</span>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                  <el-dropdown-item v-if="userStore.isAdmin" command="admin">后台管理</el-dropdown-item>
                  <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>

          <el-button v-else @click="$router.push('/login')">登录 / 注册</el-button>
        </div>
      </div>
    </el-header>

    <el-main class="main">
      <router-view />
    </el-main>

    <el-footer class="footer">
      校园二手交易平台 · Spring Boot 3 + Vue 3 课程实践项目
    </el-footer>
  </el-container>
  </el-config-provider>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, School, ChatDotRound } from '@element-plus/icons-vue'
import { useUserStore } from './store/user'
import { chatApi } from './api'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const keyword = ref('')

const unreadCount = ref(0)

async function loadUnread() {
  if (!userStore.isLogin) {
    unreadCount.value = 0
    return
  }
  try {
    const res = await chatApi.unread()
    unreadCount.value = res.data || 0
  } catch {
    // 忽略未读计数加载失败
  }
}

function doSearch() {
  // 回到首页并带上搜索条件
  router.push({ path: '/', query: { keyword: keyword.value || undefined } })
}

function goPublish() {
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录')
    return router.push('/login')
  }
  router.push('/publish')
}

function goChat() {
  router.push('/chat')
}

function onCommand(command) {
  if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'admin') {
    router.push('/admin')
  } else if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', { type: 'warning' })
      .then(() => {
        userStore.logout()
        ElMessage.success('已退出登录')
        if (route.meta.auth) router.push('/')
      })
      .catch(() => {})
  }
}

onMounted(() => {
  loadUnread()
  setInterval(loadUnread, 30000)
})

watch(() => userStore.isLogin, loadUnread)
</script>
