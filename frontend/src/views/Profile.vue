<template>
  <div class="profile">
    <div class="side page-card">
      <div class="me">
        <el-avatar :size="56" :src="userStore.user?.avatar">
          {{ userStore.nickname.charAt(0) }}
        </el-avatar>
        <div class="me-name">{{ userStore.nickname }}</div>
        <div class="me-sub">{{ userStore.user?.campus || '未填写校区' }}</div>
        <el-tag v-if="userStore.isAdmin" type="danger" size="small">管理员</el-tag>
      </div>

      <el-menu :default-active="active" class="menu" @select="onSelect">
        <el-menu-item index="products">
          <el-icon><Goods /></el-icon><span>我发布的</span>
        </el-menu-item>
        <el-menu-item index="favorites">
          <el-icon><Star /></el-icon><span>我的收藏</span>
        </el-menu-item>
        <el-menu-item index="bought">
          <el-icon><ShoppingCart /></el-icon><span>我买到的</span>
        </el-menu-item>
        <el-menu-item index="sold">
          <el-icon><Money /></el-icon><span>我卖出的</span>
        </el-menu-item>
        <el-menu-item index="setting">
          <el-icon><Setting /></el-icon><span>账号设置</span>
        </el-menu-item>
      </el-menu>
    </div>

    <div class="content">
      <!-- 我发布的 -->
      <div v-if="active === 'products'" class="page-card" v-loading="loading">
        <div class="section-title">
          <span>我发布的商品</span>
          <el-button type="primary" size="small" :icon="Plus" @click="$router.push('/publish')">
            发布新商品
          </el-button>
        </div>

        <el-table v-if="products.length" :data="products" style="width: 100%">
          <el-table-column label="商品" min-width="240">
            <template #default="{ row }">
              <div class="cell-product" @click="$router.push(`/product/${row.id}`)">
                <el-image v-if="row.coverImage" :src="row.coverImage" class="thumb" fit="cover" />
                <div v-else class="thumb thumb-empty">无图</div>
                <span class="cell-title">{{ row.title }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="价格" width="100">
            <template #default="{ row }"><span class="price">¥{{ row.price }}</span></template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="statusType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="浏览" width="80" prop="viewCount" />
          <el-table-column label="操作" width="200">
            <template #default="{ row }">
              <el-button link type="primary" size="small" @click="$router.push(`/edit/${row.id}`)">
                编辑
              </el-button>
              <el-button
                v-if="row.status === 1"
                link
                type="warning"
                size="small"
                @click="changeProductStatus(row, 'offline')"
              >下架</el-button>
              <el-button
                v-else-if="row.status === 3"
                link
                type="success"
                size="small"
                @click="changeProductStatus(row, 'online')"
              >上架</el-button>
              <el-button link type="danger" size="small" @click="removeProduct(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-else description="还没有发布过商品" />

        <div v-if="productTotal > productQuery.pageSize" class="pager">
          <el-pagination
            v-model:current-page="productQuery.pageNum"
            :page-size="productQuery.pageSize"
            :total="productTotal"
            layout="prev, pager, next"
            @current-change="loadProducts"
          />
        </div>
      </div>

      <!-- 我的收藏 -->
      <div v-else-if="active === 'favorites'" class="page-card" v-loading="loading">
        <div class="section-title">我的收藏</div>
        <div v-if="favorites.length" class="fav-list">
          <div v-for="f in favorites" :key="f.id" class="fav-item" @click="$router.push(`/product/${f.productId}`)">
            <el-image v-if="f.productCover" :src="f.productCover" class="thumb" fit="cover" />
            <div v-else class="thumb thumb-empty">无图</div>
            <div class="fav-info">
              <div class="cell-title">{{ f.productTitle }}</div>
              <div class="price">¥{{ f.productPrice }}</div>
            </div>
            <el-button link type="danger" size="small" @click.stop="unfavorite(f)">取消收藏</el-button>
          </div>
        </div>
        <el-empty v-else description="还没有收藏任何商品" />
      </div>

      <!-- 我买到的 / 我卖出的 -->
      <div v-else-if="active === 'bought' || active === 'sold'" class="page-card" v-loading="loading">
        <div class="section-title">
          <span>{{ active === 'bought' ? '我买到的' : '我卖出的' }}</span>
          <el-radio-group v-model="orderQuery.status" size="small" @change="loadOrders">
            <el-radio-button :value="null">全部</el-radio-button>
            <el-radio-button :value="0">待支付</el-radio-button>
            <el-radio-button :value="1">待确认</el-radio-button>
            <el-radio-button :value="2">交易中</el-radio-button>
            <el-radio-button :value="3">已完成</el-radio-button>
            <el-radio-button :value="4">已取消</el-radio-button>
          </el-radio-group>
        </div>

        <el-table v-if="orders.length" :data="orders" style="width: 100%">
          <el-table-column label="订单号" prop="orderNo" width="200" />
          <el-table-column label="商品" min-width="200">
            <template #default="{ row }">
              <span class="cell-title link" @click="$router.push(`/product/${row.productId}`)">
                {{ row.productTitle }}
              </span>
            </template>
          </el-table-column>
          <el-table-column :label="active === 'bought' ? '卖家' : '买家'" width="100">
            <template #default="{ row }">
              {{ active === 'bought' ? row.sellerNickname : row.buyerNickname }}
            </template>
          </el-table-column>
          <el-table-column label="金额" width="100">
            <template #default="{ row }"><span class="price">¥{{ row.amount }}</span></template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="orderStatusType(row.status)" size="small">
                {{ orderStatusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="联系方式" width="130" prop="contact" />
          <el-table-column label="操作" width="180">
            <template #default="{ row }">
              <el-button
                v-if="active === 'bought' && row.status === 0"
                link
                type="warning"
                size="small"
                @click="openPay(row)"
              >去支付</el-button>
              <el-button
                v-if="active === 'sold' && row.status === 1"
                link
                type="primary"
                size="small"
                @click="confirmOrder(row)"
              >确认交易</el-button>
              <el-button
                v-if="active === 'bought' && (row.status === 1 || row.status === 2)"
                link
                type="success"
                size="small"
                @click="finishOrder(row)"
              >确认完成</el-button>
              <el-button
                v-if="row.status === 0 || row.status === 1 || row.status === 2"
                link
                type="danger"
                size="small"
                @click="cancelOrder(row)"
              >取消订单</el-button>
              <span v-if="row.status === 3 || row.status === 4" class="muted">—</span>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-else description="暂无订单" />

        <div v-if="orderTotal > orderQuery.pageSize" class="pager">
          <el-pagination
            v-model:current-page="orderQuery.pageNum"
            :page-size="orderQuery.pageSize"
            :total="orderTotal"
            layout="prev, pager, next"
            @current-change="loadOrders"
          />
        </div>
      </div>

      <!-- 账号设置 -->
      <div v-else-if="active === 'setting'" class="page-card">
        <div class="section-title">账号设置</div>

        <el-form :model="profileForm" label-width="90px" class="setting-form">
          <el-divider content-position="left">基本资料</el-divider>
          <el-form-item label="昵称">
            <el-input v-model="profileForm.nickname" maxlength="20" style="width: 260px" />
          </el-form-item>
          <el-form-item label="手机号">
            <el-input v-model="profileForm.phone" maxlength="11" style="width: 260px" />
          </el-form-item>
          <el-form-item label="所在校区">
            <el-input v-model="profileForm.campus" maxlength="100" style="width: 260px" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="saving" @click="saveProfile">保存资料</el-button>
          </el-form-item>

          <el-divider content-position="left">修改密码</el-divider>
          <el-form-item label="原密码">
            <el-input v-model="pwdForm.oldPassword" type="password" show-password style="width: 260px" />
          </el-form-item>
          <el-form-item label="新密码">
            <el-input v-model="pwdForm.newPassword" type="password" show-password style="width: 260px" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="saving" @click="savePassword">修改密码</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 模拟支付弹窗 -->
      <el-dialog v-model="payVisible" title="模拟支付" width="420px">
        <div v-if="payOrder" class="pay-box">
          <div class="pay-row"><span>商品</span><b>{{ payOrder.productTitle }}</b></div>
          <div class="pay-row"><span>订单号</span><b>{{ payOrder.orderNo }}</b></div>
          <div class="pay-row pay-amount"><span>支付金额</span><b class="price">¥{{ payOrder.amount }}</b></div>
          <p class="pay-tip">这是课程演示用的模拟支付，点击「确认支付」即可完成，不会产生真实扣款。</p>
        </div>
        <template #footer>
          <el-button @click="payVisible = false">取消</el-button>
          <el-button type="primary" :loading="paying" @click="confirmPay">确认支付</el-button>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { favoriteApi, orderApi, productApi, userApi } from '../api'
import { useUserStore } from '../store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const active = ref(route.query.tab || 'products')
const loading = ref(false)
const saving = ref(false)

const products = ref([])
const productTotal = ref(0)
const productQuery = reactive({ pageNum: 1, pageSize: 8 })

const favorites = ref([])

const orders = ref([])
const orderTotal = ref(0)
const orderQuery = reactive({ pageNum: 1, pageSize: 10, status: null })

const profileForm = reactive({
  nickname: userStore.user?.nickname || '',
  phone: userStore.user?.phone || '',
  campus: userStore.user?.campus || ''
})
const pwdForm = reactive({ oldPassword: '', newPassword: '' })

const payVisible = ref(false)
const payOrder = ref(null)
const paying = ref(false)

function openPay(row) {
  payOrder.value = row
  payVisible.value = true
}

async function confirmPay() {
  if (!payOrder.value) return
  paying.value = true
  try {
    await orderApi.pay(payOrder.value.id)
    payVisible.value = false
    ElMessage.success('支付成功，等待卖家确认')
    loadOrders()
  } catch {
    // 错误已由响应拦截器统一提示
  } finally {
    paying.value = false
  }
}

function statusType(s) {
  return { 0: 'warning', 1: 'success', 2: 'info', 3: 'info' }[s] || 'info'
}
function statusText(s) {
  return { 0: '待审核', 1: '在售中', 2: '已售出', 3: '已下架' }[s] || '未知'
}
function orderStatusType(s) {
  return { 0: 'warning', 1: 'primary', 2: 'warning', 3: 'success', 4: 'info' }[s] || 'info'
}
function orderStatusText(s) {
  return { 0: '待支付', 1: '待确认', 2: '交易中', 3: '已完成', 4: '已取消' }[s] || '未知'
}

function onSelect(index) {
  active.value = index
  router.replace({ query: { tab: index } })
}

async function loadProducts() {
  loading.value = true
  try {
    const res = await productApi.mine({
      pageNum: productQuery.pageNum,
      pageSize: productQuery.pageSize
    })
    products.value = res.data.records
    productTotal.value = res.data.total
  } finally {
    loading.value = false
  }
}

async function loadFavorites() {
  loading.value = true
  try {
    const res = await favoriteApi.list({ pageNum: 1, pageSize: 50 })
    favorites.value = res.data.records
  } finally {
    loading.value = false
  }
}

async function loadOrders() {
  loading.value = true
  try {
    const res = await orderApi.list({
      role: active.value === 'bought' ? 'buy' : 'sell',
      status: orderQuery.status,
      pageNum: orderQuery.pageNum,
      pageSize: orderQuery.pageSize
    })
    orders.value = res.data.records
    orderTotal.value = res.data.total
  } finally {
    loading.value = false
  }
}

function refresh() {
  if (active.value === 'products') loadProducts()
  else if (active.value === 'favorites') loadFavorites()
  else if (active.value === 'bought' || active.value === 'sold') loadOrders()
}

async function changeProductStatus(row, action) {
  await productApi[action](row.id)
  ElMessage.success(action === 'offline' ? '已下架' : '已重新上架')
  loadProducts()
}

async function removeProduct(row) {
  await ElMessageBox.confirm('删除后无法恢复，确定删除吗？', '提示', { type: 'warning' })
  await productApi.remove(row.id)
  ElMessage.success('已删除')
  loadProducts()
}

async function unfavorite(f) {
  await favoriteApi.toggle(f.productId)
  ElMessage.success('已取消收藏')
  loadFavorites()
}

async function confirmOrder(row) {
  await orderApi.confirm(row.id)
  ElMessage.success('已确认交易')
  loadOrders()
}

async function finishOrder(row) {
  await ElMessageBox.confirm('确认已收到商品并完成交易？', '提示', { type: 'warning' })
  await orderApi.finish(row.id)
  ElMessage.success('交易完成')
  loadOrders()
}

async function cancelOrder(row) {
  await ElMessageBox.confirm('确定取消该订单吗？商品将重新上架。', '提示', { type: 'warning' })
  await orderApi.cancel(row.id)
  ElMessage.success('订单已取消')
  loadOrders()
}

async function saveProfile() {
  saving.value = true
  try {
    await userApi.updateProfile(profileForm)
    await userStore.fetchMe()
    ElMessage.success('资料已保存')
  } finally {
    saving.value = false
  }
}

async function savePassword() {
  if (!pwdForm.oldPassword || !pwdForm.newPassword) {
    return ElMessage.warning('请填写原密码和新密码')
  }
  saving.value = true
  try {
    await userApi.changePassword(pwdForm)
    ElMessage.success('密码修改成功')
    pwdForm.oldPassword = ''
    pwdForm.newPassword = ''
  } finally {
    saving.value = false
  }
}

watch(active, refresh)

onMounted(refresh)
</script>

<style scoped>
.profile {
  display: flex;
  gap: 16px;
  align-items: flex-start;
}

.side {
  width: 232px;
  flex-shrink: 0;
  padding: 0;
  overflow: hidden;
  border-radius: var(--radius);
  border: 1px solid var(--border);
  box-shadow: var(--shadow-sm);
  background: var(--surface);
}

.me {
  padding: 26px 16px 20px;
  text-align: center;
  background: var(--brand-gradient);
  color: #fff;
}

.me :deep(.el-avatar) {
  border: 3px solid rgba(255, 255, 255, 0.7);
  background: #fff;
  color: var(--brand);
}

.me-name {
  margin-top: 12px;
  font-size: 16px;
  font-weight: 700;
}

.me-sub {
  font-size: 12px;
  opacity: 0.85;
  margin: 4px 0 8px;
}

.menu {
  border-right: none;
  padding: 8px;
}
.menu :deep(.el-menu-item.is-active) {
  background: var(--el-color-primary-light-9);
  border-radius: 10px;
  font-weight: 600;
}
.menu :deep(.el-menu-item) {
  border-radius: 10px;
  margin-bottom: 4px;
}

.content {
  flex: 1;
  min-width: 0;
}

.cell-product {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
}

.thumb {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  flex-shrink: 0;
  object-fit: cover;
}

.thumb-empty {
  background: var(--surface-2);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  color: #b0b3b8;
}

.cell-title {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.link {
  color: var(--brand);
  cursor: pointer;
}

.fav-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.fav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: all 0.2s;
}
.fav-item:hover {
  border-color: var(--brand-light);
  box-shadow: var(--shadow-sm);
}

.fav-info {
  flex: 1;
  min-width: 0;
}

.pager {
  display: flex;
  justify-content: center;
  margin-top: 18px;
}

.muted {
  color: var(--text-3);
  font-size: 13px;
}

.setting-form {
  max-width: 520px;
}

.pay-box {
  font-size: 14px;
  color: var(--text-2);
}
.pay-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px dashed var(--border);
}
.pay-amount .price {
  font-size: 22px;
}
.pay-tip {
  margin-top: 14px;
  font-size: 12px;
  color: var(--text-3);
  line-height: 1.6;
  background: var(--surface-2);
  padding: 10px 12px;
  border-radius: 8px;
}

@media (max-width: 760px) {
  .profile { flex-direction: column; }
  .side { width: 100%; }
}
</style>
