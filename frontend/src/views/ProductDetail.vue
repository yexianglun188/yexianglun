<template>
  <div v-loading="loading">
    <div v-if="product" class="detail-card">
      <div class="gallery">
        <el-carousel v-if="images.length" height="360px" indicator-position="outside">
          <el-carousel-item v-for="(img, i) in images" :key="i">
            <el-image
              :src="img"
              fit="contain"
              class="big-img"
              :preview-src-list="images"
              :initial-index="i"
              preview-teleported
            />
          </el-carousel-item>
        </el-carousel>
        <div v-else class="no-img">暂无图片</div>
      </div>

      <div class="info">
        <h1 class="title">{{ product.title }}</h1>

        <div class="price-row">
          <span class="price">¥{{ product.price }}</span>
          <span v-if="product.originalPrice" class="price-origin">原价 ¥{{ product.originalPrice }}</span>
          <el-tag v-if="product.status === 2" type="info">已售出</el-tag>
          <el-tag v-else-if="product.status === 3" type="warning">已下架</el-tag>
          <el-tag v-else type="success">在售中</el-tag>
        </div>

        <el-descriptions :column="2" border class="desc">
          <el-descriptions-item label="成色">{{ product.itemCondition || '-' }}</el-descriptions-item>
          <el-descriptions-item label="分类">{{ product.categoryName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="交易地点">{{ product.tradePlace || '面议' }}</el-descriptions-item>
          <el-descriptions-item label="浏览">{{ product.viewCount }} 次</el-descriptions-item>
        </el-descriptions>

        <div class="seller">
          <el-avatar :size="42" :src="product.sellerAvatar">
            {{ (product.sellerNickname || '同').charAt(0) }}
          </el-avatar>
          <div>
            <div class="seller-name">{{ product.sellerNickname || '匿名同学' }}</div>
            <div class="seller-campus">{{ product.sellerCampus || '校内交易' }}</div>
          </div>
        </div>

        <div class="actions">
          <el-button
            type="primary"
            size="large"
            :disabled="product.status !== 1 || isOwner"
            @click="openOrder"
          >
            {{ isOwner ? '这是你发布的商品' : product.status === 1 ? '立即购买' : '已售出' }}
          </el-button>
          <el-button
            size="large"
            :type="product.favorited ? 'warning' : 'default'"
            :icon="Star"
            @click="toggleFav"
          >
            {{ product.favorited ? '已收藏' : '收藏' }}
          </el-button>
          <el-button v-if="!isOwner" size="large" :icon="ChatDotRound" @click="goChat">
            私信卖家
          </el-button>
        </div>

        <div v-if="isOwner" class="owner-bar">
          <el-button size="small" @click="$router.push(`/edit/${product.id}`)">编辑商品</el-button>
          <el-button v-if="product.status === 1" size="small" @click="changeStatus('offline')">下架</el-button>
          <el-button v-else-if="product.status === 3" size="small" @click="changeStatus('online')">重新上架</el-button>
          <el-button size="small" type="danger" @click="removeProduct">删除</el-button>
        </div>
      </div>
    </div>

    <div v-if="product" class="page-card block-card">
      <div class="section-title">商品描述</div>
      <p class="desc-text">{{ product.description || '卖家很懒，什么都没写～' }}</p>
    </div>

    <div v-if="product" class="page-card block-card">
      <div class="section-title">留言（{{ comments.length }}）</div>

      <div class="comment-input">
        <el-input
          v-model="commentText"
          type="textarea"
          :rows="2"
          maxlength="500"
          show-word-limit
          placeholder="想问点什么？比如「还在吗」「能便宜点吗」"
        />
        <el-button type="primary" :disabled="!commentText.trim()" @click="submitComment">
          发表留言
        </el-button>
      </div>

      <div v-if="comments.length" class="comment-list">
        <div v-for="c in comments" :key="c.id" class="comment-item">
          <el-avatar :size="36" :src="c.userAvatar">{{ (c.userNickname || '同').charAt(0) }}</el-avatar>
          <div class="comment-body">
            <div class="comment-head">
              <span class="nick">{{ c.userNickname || '匿名同学' }}</span>
              <span class="time">{{ formatTime(c.createTime) }}</span>
              <el-button v-if="canDelete(c)" link type="danger" size="small" @click="removeComment(c)">
                删除
              </el-button>
            </div>
            <div class="comment-content">{{ c.content }}</div>
          </div>
        </div>
      </div>
      <el-empty v-else description="还没有人留言，来问第一个问题吧" :image-size="80" />
    </div>

    <el-dialog v-model="orderVisible" title="确认下单" width="440px">
      <el-form :model="orderForm" label-width="82px">
        <el-form-item label="商品">
          <span>{{ product?.title }}</span>
        </el-form-item>
        <el-form-item label="成交价">
          <span class="price">¥{{ product?.price }}</span>
        </el-form-item>
        <el-form-item label="联系方式" required>
          <el-input v-model="orderForm.contact" placeholder="手机号 / 微信 / QQ" />
        </el-form-item>
        <el-form-item label="留言">
          <el-input
            v-model="orderForm.remark"
            type="textarea"
            :rows="2"
            placeholder="约个交易时间和地点（选填）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="orderVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitOrder">确认下单</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Star, ChatDotRound } from '@element-plus/icons-vue'
import { commentApi, favoriteApi, orderApi, productApi } from '../api'
import { useUserStore } from '../store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const submitting = ref(false)
const product = ref(null)
const comments = ref([])
const commentText = ref('')
const orderVisible = ref(false)

const orderForm = reactive({ contact: '', remark: '' })

const images = computed(() => {
  if (product.value?.imageList?.length) return product.value.imageList
  return product.value?.coverImage ? [product.value.coverImage] : []
})

const isOwner = computed(() => userStore.user?.id === product.value?.userId)

function formatTime(t) {
  if (!t) return ''
  return String(t).replace('T', ' ').slice(0, 16)
}

async function loadProduct() {
  loading.value = true
  try {
    const res = await productApi.detail(route.params.id)
    product.value = res.data
  } catch {
    product.value = null
  } finally {
    loading.value = false
  }
}

async function loadComments() {
  try {
    const res = await commentApi.listByProduct(route.params.id)
    comments.value = res.data
  } catch {
    comments.value = []
  }
}

function requireLogin() {
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录')
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return false
  }
  return true
}

async function toggleFav() {
  if (!requireLogin()) return
  const res = await favoriteApi.toggle(product.value.id)
  product.value.favorited = res.data
}

function openOrder() {
  if (!requireLogin()) return
  orderForm.contact = userStore.user?.phone || ''
  orderForm.remark = ''
  orderVisible.value = true
}

function goChat() {
  if (!requireLogin()) return
  router.push({
    path: '/chat',
    query: { peerId: product.value.userId, productId: product.value.id }
  })
}

async function submitOrder() {
  if (!orderForm.contact.trim()) {
    return ElMessage.warning('请填写联系方式')
  }
  submitting.value = true
  try {
    await orderApi.create({
      productId: product.value.id,
      contact: orderForm.contact,
      remark: orderForm.remark
    })
    orderVisible.value = false
    ElMessage.success('下单成功，请到「个人中心 - 我买到的」完成支付')
    loadProduct()
  } catch {
    loadProduct()
  } finally {
    submitting.value = false
  }
}

async function submitComment() {
  if (!requireLogin()) return
  await commentApi.add({
    productId: product.value.id,
    content: commentText.value.trim()
  })
  commentText.value = ''
  ElMessage.success('留言成功')
  loadComments()
}

function canDelete(c) {
  return userStore.user?.id === c.userId || userStore.isAdmin
}

async function removeComment(c) {
  await ElMessageBox.confirm('确定删除这条留言吗？', '提示', { type: 'warning' })
  await commentApi.remove(c.id)
  ElMessage.success('已删除')
  loadComments()
}

async function changeStatus(action) {
  await productApi[action](product.value.id)
  ElMessage.success(action === 'offline' ? '已下架' : '已重新上架')
  loadProduct()
}

async function removeProduct() {
  await ElMessageBox.confirm('删除后无法恢复，确定删除吗？', '删除商品', { type: 'warning' })
  await productApi.remove(product.value.id)
  ElMessage.success('已删除')
  router.push('/profile')
}

onMounted(() => {
  loadProduct()
  loadComments()
})
</script>

<style scoped>
.detail-card {
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  padding: 24px;
  display: flex;
  gap: 32px;
  align-items: flex-start;
  box-shadow: var(--shadow-sm);
}

.gallery {
  width: 440px;
  flex-shrink: 0;
  border-radius: var(--radius-md);
  overflow: hidden;
  background: var(--surface-2);
}

.big-img {
  width: 100%;
  height: 360px;
  background: #fafafa;
}

.no-img {
  width: 440px;
  height: 360px;
  background: linear-gradient(135deg, #eef2f8, #e3e9f2);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #b0b3b8;
  border-radius: var(--radius-md);
}

.info {
  flex: 1;
  min-width: 0;
  position: sticky;
  top: 84px;
}

.title {
  font-size: 22px;
  line-height: 1.5;
  margin-bottom: 16px;
  font-weight: 700;
}

.price-row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 18px;
  background: linear-gradient(135deg, #fff1f0, #ffe9ed);
  border: 1px solid #ffd9de;
  border-radius: var(--radius-md);
}

.price {
  color: var(--price);
  font-size: 30px;
  font-weight: 800;
}

.desc {
  margin-top: 20px;
}

.seller {
  margin-top: 20px;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 14px;
  background: var(--surface-2);
  border-radius: var(--radius-md);
}

.seller-name {
  font-size: 14px;
  font-weight: 600;
}

.seller-campus {
  font-size: 12px;
  color: var(--text-3);
  margin-top: 2px;
}

.actions {
  margin-top: 24px;
  display: flex;
  gap: 12px;
}

.actions .el-button {
  flex: 1;
  height: 46px;
  font-size: 15px;
}

.owner-bar {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px dashed var(--border-strong);
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.block-card {
  margin-top: 18px;
}

.desc-text {
  white-space: pre-wrap;
  line-height: 1.9;
  color: var(--text-2);
}

.comment-input {
  display: flex;
  gap: 12px;
  align-items: flex-end;
  margin-bottom: 22px;
}

.comment-list {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.comment-item {
  display: flex;
  gap: 12px;
}

.comment-body {
  flex: 1;
  min-width: 0;
}

.comment-head {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 13px;
}

.comment-head .nick {
  font-weight: 600;
}

.comment-head .time {
  color: var(--text-3);
  font-size: 12px;
}

.comment-content {
  margin-top: 4px;
  line-height: 1.7;
  color: var(--text-2);
  word-break: break-word;
}

@media (max-width: 820px) {
  .detail-card {
    flex-direction: column;
    gap: 18px;
  }
  .gallery,
  .no-img {
    width: 100%;
  }
  .info {
    position: static;
  }
}
</style>
