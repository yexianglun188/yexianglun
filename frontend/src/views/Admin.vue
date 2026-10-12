<template>
  <div class="page-card">
    <div class="section-title">后台管理</div>

    <el-tabs v-model="tab" @tab-change="onTabChange">
      <!-- 数据看板 -->
      <el-tab-pane label="数据看板" name="dashboard">
        <div v-loading="loading" class="dash">
          <div class="stat-cards">
            <div class="stat-card">
              <div class="stat-ic ic-blue">👥</div>
              <div class="stat-num">{{ dashboard?.userCount ?? '-' }}</div>
              <div class="stat-label">用户总数</div>
            </div>
            <div class="stat-card">
              <div class="stat-ic ic-purple">📦</div>
              <div class="stat-num">{{ dashboard?.productCount ?? '-' }}</div>
              <div class="stat-label">商品总数</div>
            </div>
            <div class="stat-card">
              <div class="stat-ic ic-green">🟢</div>
              <div class="stat-num">{{ dashboard?.onSaleCount ?? '-' }}</div>
              <div class="stat-label">在售商品</div>
            </div>
            <div class="stat-card">
              <div class="stat-ic ic-orange">✅</div>
              <div class="stat-num">{{ dashboard?.soldCount ?? '-' }}</div>
              <div class="stat-label">已售商品</div>
            </div>
            <div class="stat-card">
              <div class="stat-ic ic-cyan">🧾</div>
              <div class="stat-num">{{ dashboard?.orderCount ?? '-' }}</div>
              <div class="stat-label">订单总数</div>
            </div>
            <div class="stat-card">
              <div class="stat-ic ic-teal">🏆</div>
              <div class="stat-num">{{ dashboard?.finishedCount ?? '-' }}</div>
              <div class="stat-label">已完成订单</div>
            </div>
            <div class="stat-card accent">
              <div class="stat-ic ic-gold">💰</div>
              <div class="stat-num">¥{{ dashboard?.totalGmv ?? '0' }}</div>
              <div class="stat-label">累计成交额</div>
            </div>
          </div>

          <div class="charts">
            <div class="chart-card">
              <div class="chart-title">近 7 日订单量</div>
              <div class="bars">
                <div v-for="(t, i) in dashboard?.trend" :key="i" class="bar-col">
                  <div class="bar" :style="{ height: barHeight(t.count) }"></div>
                  <div class="bar-val">{{ t.count }}</div>
                  <div class="bar-label">{{ (t.day || '').slice(5) }}</div>
                </div>
                <div v-if="!dashboard?.trend?.length" class="no-data">暂无数据</div>
              </div>
            </div>

            <div class="chart-card">
              <div class="chart-title">分类商品分布</div>
              <div class="cat-bars">
                <div v-for="(c, i) in dashboard?.categories" :key="i" class="cat-row">
                  <div class="cat-name">{{ c.name || '未分类' }}</div>
                  <div class="cat-track"><div class="cat-fill" :style="{ width: catWidth(c.count) }"></div></div>
                  <div class="cat-val">{{ c.count }}</div>
                </div>
                <div v-if="!dashboard?.categories?.length" class="no-data">暂无数据</div>
              </div>
            </div>
          </div>
        </div>
      </el-tab-pane>

      <!-- 用户管理 -->
      <el-tab-pane label="用户管理" name="users">
        <div class="toolbar">
          <el-input
            v-model="userKeyword"
            placeholder="搜索用户名 / 昵称"
            clearable
            style="width: 260px"
            @keyup.enter="loadUsers"
          >
            <template #append>
              <el-button :icon="Search" @click="loadUsers" />
            </template>
          </el-input>
        </div>

        <el-table v-loading="loading" :data="users" style="width: 100%">
          <el-table-column prop="id" label="ID" width="70" />
          <el-table-column prop="username" label="用户名" width="140" />
          <el-table-column prop="nickname" label="昵称" width="140" />
          <el-table-column prop="phone" label="手机号" width="140" />
          <el-table-column prop="campus" label="校区" width="120" />
          <el-table-column label="角色" width="100">
            <template #default="{ row }">
              <el-tag :type="row.role === 1 ? 'danger' : 'info'" size="small">
                {{ row.role === 1 ? '管理员' : '普通用户' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'warning'" size="small">
                {{ row.status === 1 ? '正常' : '已禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="注册时间" width="170">
            <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="120">
            <template #default="{ row }">
              <el-button
                v-if="row.role !== 1"
                link
                :type="row.status === 1 ? 'danger' : 'success'"
                size="small"
                @click="toggleUserStatus(row)"
              >{{ row.status === 1 ? '禁用' : '启用' }}</el-button>
              <span v-else class="muted">—</span>
            </template>
          </el-table-column>
        </el-table>

        <div v-if="userTotal > userQuery.pageSize" class="pager">
          <el-pagination
            v-model:current-page="userQuery.pageNum"
            :page-size="userQuery.pageSize"
            :total="userTotal"
            layout="prev, pager, next, total"
            @current-change="loadUsers"
          />
        </div>
      </el-tab-pane>

      <!-- 商品管理 -->
      <el-tab-pane label="商品管理" name="products">
        <div class="toolbar">
          <el-input
            v-model="productKeyword"
            placeholder="搜索商品标题"
            clearable
            style="width: 260px"
            @keyup.enter="loadProducts"
          >
            <template #append>
              <el-button :icon="Search" @click="loadProducts" />
            </template>
          </el-input>

          <el-radio-group v-model="productQuery.status" @change="reloadProducts">
            <el-radio-button :value="null">全部</el-radio-button>
            <el-radio-button :value="1">在售</el-radio-button>
            <el-radio-button :value="2">已售</el-radio-button>
            <el-radio-button :value="3">已下架</el-radio-button>
          </el-radio-group>
        </div>

        <el-table v-loading="loading" :data="products" style="width: 100%">
          <el-table-column prop="id" label="ID" width="70" />
          <el-table-column label="商品" min-width="240">
            <template #default="{ row }">
              <span class="link" @click="openProduct(row.id)">{{ row.title }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="sellerNickname" label="卖家" width="110" />
          <el-table-column prop="categoryName" label="分类" width="110" />
          <el-table-column label="价格" width="100">
            <template #default="{ row }"><span class="price">¥{{ row.price }}</span></template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="statusType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="viewCount" label="浏览" width="80" />
          <el-table-column label="发布时间" width="170">
            <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="110">
            <template #default="{ row }">
              <el-button
                v-if="row.status !== 3"
                link
                type="danger"
                size="small"
                @click="offlineProduct(row)"
              >下架</el-button>
              <span v-else class="muted">—</span>
            </template>
          </el-table-column>
        </el-table>

        <div v-if="productTotal > productQuery.pageSize" class="pager">
          <el-pagination
            v-model:current-page="productQuery.pageNum"
            :page-size="productQuery.pageSize"
            :total="productTotal"
            layout="prev, pager, next, total"
            @current-change="loadProducts"
          />
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { adminApi } from '../api'

const router = useRouter()

const tab = ref('dashboard')
const loading = ref(false)

const users = ref([])
const userTotal = ref(0)
const userKeyword = ref('')
const userQuery = reactive({ pageNum: 1, pageSize: 10 })

const products = ref([])
const productTotal = ref(0)
const productKeyword = ref('')
const productQuery = reactive({ pageNum: 1, pageSize: 10, status: null })

const dashboard = ref(null)

function barHeight(count) {
  const arr = dashboard.value?.trend?.map(t => Number(t.count)) || [1]
  const max = Math.max(1, ...arr)
  const v = Number(count) || 0
  return (v / max) * 120 + 4 + 'px'
}

function catWidth(count) {
  const arr = dashboard.value?.categories?.map(c => Number(c.count)) || [1]
  const max = Math.max(1, ...arr)
  const v = Number(count) || 0
  return (v / max) * 100 + '%'
}

async function loadDashboard() {
  loading.value = true
  try {
    const res = await adminApi.dashboard()
    dashboard.value = res.data
  } finally {
    loading.value = false
  }
}

function formatTime(t) {
  if (!t) return '-'
  return String(t).replace('T', ' ').slice(0, 16)
}

function statusType(s) {
  return { 0: 'warning', 1: 'success', 2: 'info', 3: 'info' }[s] || 'info'
}

function statusText(s) {
  return { 0: '待审核', 1: '在售中', 2: '已售出', 3: '已下架' }[s] || '未知'
}

async function loadUsers() {
  loading.value = true
  try {
    const res = await adminApi.users({
      pageNum: userQuery.pageNum,
      pageSize: userQuery.pageSize,
      keyword: userKeyword.value || undefined
    })
    users.value = res.data.records
    userTotal.value = res.data.total
  } finally {
    loading.value = false
  }
}

async function loadProducts() {
  loading.value = true
  try {
    const res = await adminApi.products({
      pageNum: productQuery.pageNum,
      pageSize: productQuery.pageSize,
      status: productQuery.status,
      keyword: productKeyword.value || undefined
    })
    products.value = res.data.records
    productTotal.value = res.data.total
  } finally {
    loading.value = false
  }
}

function reloadProducts() {
  productQuery.pageNum = 1
  loadProducts()
}

function onTabChange(name) {
  if (name === 'dashboard') loadDashboard()
  else if (name === 'users') loadUsers()
  else loadProducts()
}

async function toggleUserStatus(row) {
  const next = row.status === 1 ? 0 : 1
  const action = next === 1 ? '启用' : '禁用'
  await ElMessageBox.confirm(`确定要${action}用户「${row.nickname || row.username}」吗？`, '提示', {
    type: 'warning'
  })
  await adminApi.updateUserStatus(row.id, next)
  ElMessage.success(`已${action}`)
  loadUsers()
}

async function offlineProduct(row) {
  await ElMessageBox.confirm(`确定下架商品「${row.title}」吗？`, '提示', { type: 'warning' })
  await adminApi.offlineProduct(row.id)
  ElMessage.success('已下架')
  loadProducts()
}

function openProduct(id) {
  router.push(`/product/${id}`)
}

onMounted(() => {
  loadDashboard()
  loadUsers()
})
</script>

<style scoped>
.dash {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.stat-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 16px;
}

.stat-card {
  position: relative;
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: var(--radius-md);
  padding: 18px 18px 16px;
  box-shadow: var(--shadow-sm);
  transition: transform 0.2s, box-shadow 0.2s;
  overflow: hidden;
}
.stat-card:hover {
  transform: translateY(-3px);
  box-shadow: var(--shadow);
}
.stat-card::before {
  content: "";
  position: absolute;
  left: 0;
  top: 0;
  width: 4px;
  height: 100%;
  background: var(--brand);
  opacity: 0.85;
}

.stat-ic {
  width: 38px;
  height: 38px;
  border-radius: 11px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  margin-bottom: 12px;
}
.ic-blue { background: #e8f0ff; }
.ic-purple { background: #efe9ff; }
.ic-green { background: #e3f8ef; }
.ic-orange { background: #fff1e2; }
.ic-cyan { background: #e3f6fb; }
.ic-teal { background: #e2f7f2; }
.ic-gold { background: #fff4d9; }

.stat-num {
  font-size: 26px;
  font-weight: 800;
  color: var(--text);
  line-height: 1.2;
}
.stat-label {
  margin-top: 4px;
  font-size: 13px;
  color: var(--text-3);
}

.stat-card.accent {
  background: var(--brand-gradient);
  border: none;
}
.stat-card.accent::before { display: none; }
.stat-card.accent .stat-num,
.stat-card.accent .stat-label {
  color: #fff;
}
.stat-card.accent .stat-ic {
  background: rgba(255, 255, 255, 0.22);
}

.charts {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}
@media (max-width: 860px) {
  .charts { grid-template-columns: 1fr; }
}

.chart-card {
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: var(--radius-md);
  padding: 18px 20px;
  box-shadow: var(--shadow-sm);
}
.chart-title {
  font-size: 15px;
  font-weight: 700;
  margin-bottom: 18px;
  color: var(--text);
}

.bars {
  display: flex;
  align-items: flex-end;
  gap: 10px;
  height: 160px;
  padding-top: 10px;
}
.bar-col {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-end;
  height: 100%;
}
.bar {
  width: 60%;
  max-width: 28px;
  background: var(--brand-gradient);
  border-radius: 6px 6px 0 0;
  transition: height 0.4s ease;
  min-height: 4px;
}
.bar-val {
  font-size: 12px;
  font-weight: 600;
  color: var(--text-2);
  margin-bottom: 4px;
}
.bar-label {
  font-size: 11px;
  color: var(--text-3);
  margin-top: 6px;
}

.cat-bars {
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding: 4px 0;
}
.cat-row {
  display: flex;
  align-items: center;
  gap: 12px;
}
.cat-name {
  width: 76px;
  font-size: 13px;
  color: var(--text-2);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.cat-track {
  flex: 1;
  height: 10px;
  background: var(--surface-2);
  border-radius: 999px;
  overflow: hidden;
}
.cat-fill {
  height: 100%;
  background: linear-gradient(90deg, #4f7cff, #18c2a0);
  border-radius: 999px;
  transition: width 0.4s ease;
}
.cat-val {
  width: 32px;
  text-align: right;
  font-size: 13px;
  font-weight: 600;
  color: var(--text-2);
}

.no-data {
  color: var(--text-3);
  font-size: 13px;
  padding: 30px 0;
  text-align: center;
}

.toolbar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  align-items: center;
  flex-wrap: wrap;
}

.link {
  color: var(--brand);
  cursor: pointer;
}

.muted {
  color: var(--text-3);
  font-size: 13px;
}

.pager {
  display: flex;
  justify-content: center;
  margin-top: 18px;
}
</style>
