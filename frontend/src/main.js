import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'
import { setUnauthorizedHandler } from './api'
import './style.css'

// 令牌失效时统一跳转登录页
setUnauthorizedHandler(() => {
  if (router.currentRoute.value.path !== '/login') {
    router.push('/login')
  }
})

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.mount('#app')
