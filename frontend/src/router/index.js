import { createRouter, createWebHashHistory } from 'vue-router'

const routes = [
  { path: '/', name: 'home', component: () => import('../views/Home.vue') },
  { path: '/login', name: 'login', component: () => import('../views/Login.vue') },
  { path: '/product/:id', name: 'productDetail', component: () => import('../views/ProductDetail.vue') },
  { path: '/publish', name: 'publish', component: () => import('../views/Publish.vue'), meta: { auth: true } },
  { path: '/edit/:id', name: 'edit', component: () => import('../views/Publish.vue'), meta: { auth: true } },
  { path: '/profile', name: 'profile', component: () => import('../views/Profile.vue'), meta: { auth: true } },
  { path: '/chat', name: 'chat', component: () => import('../views/Chat.vue'), meta: { auth: true } },
  { path: '/admin', name: 'admin', component: () => import('../views/Admin.vue'), meta: { auth: true, admin: true } },
  { path: '/:pathMatch(.*)*', redirect: '/' }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 })
})

// 路由守卫：登录与管理员权限校验
router.beforeEach(to => {
  const token = localStorage.getItem('token')
  let user = null
  try {
    user = JSON.parse(localStorage.getItem('user') || 'null')
  } catch {
    user = null
  }

  if (to.meta.auth && !token) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  if (to.meta.admin && user?.role !== 1) {
    return { path: '/' }
  }
  return true
})

export default router
