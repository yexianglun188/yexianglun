<template>
  <div class="login-wrap">
    <div class="login-shell">
      <!-- 左侧品牌区 -->
      <div class="brand-panel">
        <div class="brand-deco"></div>
        <div class="brand-inner">
          <div class="brand-logo">
            <el-icon :size="30"><School /></el-icon>
            <span>校园二手交易平台</span>
          </div>
          <h1 class="brand-title">让闲置，遇见需要它的人</h1>
          <p class="brand-sub">同学之间的二手交易，更安心、更实惠</p>
          <ul class="brand-feats">
            <li><el-icon><CircleCheck /></el-icon> 校园实名，交易更放心</li>
            <li><el-icon><CircleCheck /></el-icon> 当面验货，少踩坑</li>
            <li><el-icon><CircleCheck /></el-icon> 闲置一键变现，简单高效</li>
          </ul>
        </div>
        <div class="brand-foot">Spring Boot 3 + Vue 3 课程实践项目</div>
      </div>

      <!-- 右侧表单区 -->
      <div class="form-panel">
        <div class="form-card">
          <div class="form-head">
            <h2>欢迎回来 👋</h2>
            <p>登录或注册，开启你的校园交易</p>
          </div>

          <el-tabs v-model="tab" stretch>
            <el-tab-pane label="登录" name="login">
              <el-form ref="loginFormRef" :model="loginForm" :rules="loginRules" label-position="top">
                <el-form-item label="用户名" prop="username">
                  <el-input v-model="loginForm.username" placeholder="请输入用户名" :prefix-icon="User" />
                </el-form-item>
                <el-form-item label="密码" prop="password">
                  <el-input
                    v-model="loginForm.password"
                    type="password"
                    placeholder="请输入密码"
                    show-password
                    :prefix-icon="Lock"
                    @keyup.enter="doLogin"
                  />
                </el-form-item>
                <el-button type="primary" class="submit" :loading="loading" @click="doLogin">
                  登 录
                </el-button>
                <div class="tip">测试账号：admin / 123456（管理员）、zhangsan / 123456（普通用户）</div>
              </el-form>
            </el-tab-pane>

            <el-tab-pane label="注册" name="register">
              <el-form ref="regFormRef" :model="regForm" :rules="regRules" label-position="top">
                <el-form-item label="用户名" prop="username">
                  <el-input v-model="regForm.username" placeholder="4-20位字母、数字或下划线" />
                </el-form-item>
                <el-form-item label="密码" prop="password">
                  <el-input v-model="regForm.password" type="password" show-password placeholder="6-20位" />
                </el-form-item>
                <el-form-item label="昵称" prop="nickname">
                  <el-input v-model="regForm.nickname" placeholder="同学们怎么称呼你" />
                </el-form-item>
                <el-form-item label="手机号" prop="phone">
                  <el-input v-model="regForm.phone" placeholder="用于交易联系（选填）" />
                </el-form-item>
                <el-form-item label="所在校区" prop="campus">
                  <el-input v-model="regForm.campus" placeholder="如：东校区（选填）" />
                </el-form-item>
                <el-button type="primary" class="submit" :loading="loading" @click="doRegister">
                  注 册
                </el-button>
              </el-form>
            </el-tab-pane>
          </el-tabs>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Lock, School, User, CircleCheck } from '@element-plus/icons-vue'
import { authApi } from '../api'
import { useUserStore } from '../store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const tab = ref('login')
const loading = ref(false)

const loginFormRef = ref()
const regFormRef = ref()

const loginForm = reactive({ username: '', password: '' })
const regForm = reactive({ username: '', password: '', nickname: '', phone: '', campus: '' })

const loginRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const regRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]{4,20}$/, message: '4-20位字母、数字或下划线', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20位', trigger: 'blur' }
  ],
  phone: [{ pattern: /^$|^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }]
}

async function doLogin() {
  await loginFormRef.value.validate()
  loading.value = true
  try {
    await userStore.login(loginForm)
    ElMessage.success('登录成功')
    router.push(route.query.redirect || '/')
  } catch {
    // 错误提示已由拦截器统一处理
  } finally {
    loading.value = false
  }
}

async function doRegister() {
  await regFormRef.value.validate()
  loading.value = true
  try {
    await authApi.register(regForm)
    ElMessage.success('注册成功，请登录')
    loginForm.username = regForm.username
    loginForm.password = ''
    tab.value = 'login'
  } catch {
    // 忽略
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-wrap {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 220px);
  padding: 30px 16px;
}

.login-shell {
  width: 880px;
  max-width: 100%;
  min-height: 540px;
  display: flex;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: var(--shadow-lg);
  background: #fff;
}

/* 左侧品牌区 */
.brand-panel {
  position: relative;
  width: 42%;
  flex-shrink: 0;
  padding: 40px 36px;
  color: #fff;
  background: var(--brand-gradient);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.brand-deco {
  position: absolute;
  right: -50px;
  top: -50px;
  width: 200px;
  height: 200px;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.2), transparent 70%);
  border-radius: 50%;
}
.brand-inner {
  position: relative;
  z-index: 1;
  flex: 1;
}
.brand-logo {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 18px;
  font-weight: 700;
}
.brand-title {
  margin-top: 40px;
  font-size: 28px;
  font-weight: 800;
  line-height: 1.4;
}
.brand-sub {
  margin-top: 14px;
  font-size: 14px;
  opacity: 0.92;
}
.brand-feats {
  list-style: none;
  margin-top: 36px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.brand-feats li {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
  background: rgba(255, 255, 255, 0.12);
  padding: 12px 14px;
  border-radius: 12px;
}
.brand-feats .el-icon {
  font-size: 18px;
}
.brand-foot {
  position: relative;
  z-index: 1;
  font-size: 12px;
  opacity: 0.8;
}

/* 右侧表单区 */
.form-panel {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 32px;
}
.form-card {
  width: 100%;
  max-width: 340px;
}
.form-head {
  margin-bottom: 18px;
}
.form-head h2 {
  font-size: 22px;
  font-weight: 700;
  color: var(--text);
}
.form-head p {
  margin-top: 6px;
  font-size: 13px;
  color: var(--text-3);
}

.submit {
  width: 100%;
  margin-top: 6px;
  height: 42px;
}

.tip {
  margin-top: 16px;
  font-size: 12px;
  color: var(--text-3);
  line-height: 1.6;
  text-align: center;
  background: var(--surface-2);
  border-radius: 8px;
  padding: 8px 10px;
}

/* 响应式：窄屏堆叠 */
@media (max-width: 720px) {
  .login-shell {
    flex-direction: column;
    min-height: auto;
  }
  .brand-panel {
    width: 100%;
  }
  .brand-title {
    margin-top: 20px;
  }
  .brand-feats {
    margin-top: 22px;
  }
}
</style>
