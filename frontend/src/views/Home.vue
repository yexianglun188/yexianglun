<template>
  <div>
    <div class="banner">
      <div class="banner-deco"></div>
      <div class="banner-text">
        <div class="banner-tag">校园闲置交易市场</div>
        <h2>校园闲置，物尽其用</h2>
        <p>把用不上的东西，转给需要的同学 · 实惠、可靠、当面交易</p>
      </div>
      <el-button class="banner-btn" size="large" :icon="Plus" @click="goPublish">我也要发布</el-button>
    </div>

    <div class="page-card filter-card">
      <div class="cats">
        <span class="cat-label">分类：</span>
        <el-tag
          class="cat"
          :effect="!query.categoryId ? 'dark' : 'plain'"
          @click="selectCategory(null)"
        >全部</el-tag>
        <el-tag
          v-for="c in categories"
          :key="c.id"
          class="cat"
          :effect="query.categoryId === c.id ? 'dark' : 'plain'"
          @click="selectCategory(c.id)"
        >{{ c.name }}</el-tag>
      </div>

      <div class="sort">
        <span class="cat-label">排序：</span>
        <el-radio-group v-model="query.sort" @change="reload">
          <el-radio-button value="latest">最新发布</el-radio-button>
          <el-radio-button value="priceAsc">价格从低到高</el-radio-button>
          <el-radio-button value="priceDesc">价格从高到低</el-radio-button>
          <el-radio-button value="hot">最多浏览</el-radio-button>
        </el-radio-group>
      </div>

      <div v-if="query.keyword" class="keyword-tip">
        搜索「<b>{{ query.keyword }}</b>」共找到 {{ total }} 件商品
        <el-button link type="primary" @click="clearKeyword">清除</el-button>
      </div>
    </div>

    <div v-loading="loading" class="list-wrap">
      <div v-if="products.length" class="grid">
        <ProductCard v-for="p in products" :key="p.id" :product="p" />
      </div>
      <div v-else-if="!loading" class="page-card">
        <el-empty description="还没有相关商品，换个关键词试试" />
      </div>
    </div>

    <div v-if="total > query.pageSize" class="pager">
      <el-pagination
        v-model:current-page="query.pageNum"
        :page-size="query.pageSize"
        :total="total"
        layout="prev, pager, next, total"
        background
        @current-change="load"
      />
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { categoryApi, productApi } from '../api'
import ProductCard from '../components/ProductCard.vue'
import { useUserStore } from '../store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const categories = ref([])
const products = ref([])
const total = ref(0)
const loading = ref(false)

const query = reactive({
  keyword: '',
  categoryId: null,
  sort: 'latest',
  pageNum: 1,
  pageSize: 12
})

async function load() {
  loading.value = true
  try {
    const res = await productApi.list({
      keyword: query.keyword || undefined,
      categoryId: query.categoryId || undefined,
      sort: query.sort,
      pageNum: query.pageNum,
      pageSize: query.pageSize
    })
    products.value = res.data.records
    total.value = res.data.total
  } catch {
    products.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function reload() {
  query.pageNum = 1
  load()
}

function selectCategory(id) {
  query.categoryId = id
  reload()
}

function clearKeyword() {
  query.keyword = ''
  router.push({ path: '/' })
}

function goPublish() {
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录')
    return router.push('/login')
  }
  router.push('/publish')
}

watch(
  () => route.query.keyword,
  kw => {
    query.keyword = kw || ''
    reload()
  }
)

onMounted(async () => {
  query.keyword = route.query.keyword || ''
  try {
    const res = await categoryApi.list()
    categories.value = res.data
  } catch {
    categories.value = []
  }
  load()
})
</script>

<style scoped>
.banner {
  position: relative;
  overflow: hidden;
  background: var(--brand-gradient);
  border-radius: var(--radius);
  padding: 34px 38px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: #fff;
  margin-bottom: 18px;
  box-shadow: 0 14px 36px rgba(58, 109, 240, 0.28);
}
.banner::after {
  content: "";
  position: absolute;
  right: -60px;
  top: -60px;
  width: 220px;
  height: 220px;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.22), transparent 70%);
  border-radius: 50%;
}
.banner-deco {
  position: absolute;
  right: 40px;
  bottom: -30px;
  width: 160px;
  height: 160px;
  border: 14px solid rgba(255, 255, 255, 0.12);
  border-radius: 28px;
  transform: rotate(12deg);
}

.banner-tag {
  display: inline-block;
  font-size: 12px;
  letter-spacing: 1px;
  background: rgba(255, 255, 255, 0.18);
  border: 1px solid rgba(255, 255, 255, 0.35);
  padding: 3px 12px;
  border-radius: 999px;
  margin-bottom: 12px;
}

.banner-text h2 {
  font-size: 26px;
  font-weight: 700;
  margin-bottom: 8px;
  position: relative;
  z-index: 1;
}

.banner-text p {
  font-size: 14px;
  opacity: 0.92;
  position: relative;
  z-index: 1;
}

.banner-btn {
  position: relative;
  z-index: 1;
  background: #fff !important;
  color: var(--brand) !important;
  border: none !important;
  font-weight: 600;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.12);
}

.filter-card {
  display: flex;
  flex-direction: column;
  gap: 16px;
  border-radius: var(--radius);
  background: var(--surface);
  border: 1px solid var(--border);
  box-shadow: var(--shadow-sm);
}

.cats,
.sort {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.cat-label {
  font-size: 14px;
  color: var(--text-2);
  font-weight: 500;
}

.cat {
  cursor: pointer;
  border-radius: 999px !important;
  transition: all 0.2s;
}

.keyword-tip {
  font-size: 13px;
  color: var(--text-2);
}

.list-wrap {
  margin-top: 18px;
  min-height: 200px;
}

.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(216px, 1fr));
  gap: 18px;
}

.pager {
  display: flex;
  justify-content: center;
  margin-top: 26px;
}
</style>
