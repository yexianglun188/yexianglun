<template>
  <div class="card hover-lift" @click="goDetail">
    <div class="cover">
      <el-image v-if="product.coverImage" :src="product.coverImage" fit="cover" class="img" lazy>
        <template #error>
          <div class="no-img"><el-icon :size="30"><Goods /></el-icon></div>
        </template>
      </el-image>
      <div v-else class="no-img"><el-icon :size="30"><Goods /></el-icon></div>

      <span v-if="product.categoryName" class="cat-chip">{{ product.categoryName }}</span>
      <el-tag v-if="product.status === 2" class="status-tag" type="info" size="small" effect="dark">已售罄</el-tag>
      <el-tag v-else-if="product.status === 3" class="status-tag" type="warning" size="small" effect="dark">已下架</el-tag>
    </div>

    <div class="info">
      <div class="title" :title="product.title">{{ product.title }}</div>

      <div class="meta">
        <span class="price">¥{{ product.price }}</span>
        <span v-if="product.originalPrice" class="price-origin">¥{{ product.originalPrice }}</span>
      </div>

      <div class="bottom">
        <span class="cond">{{ product.itemCondition || '成色良好' }}</span>
        <span class="campus">{{ product.sellerCampus || '校内交易' }}</span>
      </div>

      <div class="seller">
        <el-icon class="user-ic"><User /></el-icon>
        <span class="seller-name">{{ product.sellerNickname || '匿名同学' }}</span>
        <span class="views">{{ product.viewCount || 0 }} 浏览</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { Goods, User } from '@element-plus/icons-vue'

const props = defineProps({
  product: { type: Object, required: true }
})

const router = useRouter()

function goDetail() {
  router.push(`/product/${props.product.id}`)
}
</script>

<style scoped>
.card {
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  overflow: hidden;
  cursor: pointer;
  box-shadow: var(--shadow-sm);
}

.cover {
  position: relative;
  width: 100%;
  aspect-ratio: 4 / 3;
  background: var(--surface-2);
}

.img {
  width: 100%;
  height: 100%;
  display: block;
}

.no-img {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #c2c8d4;
  background: linear-gradient(135deg, #eef2f8, #e3e9f2);
}

.cat-chip {
  position: absolute;
  top: 10px;
  left: 10px;
  background: rgba(31, 37, 51, 0.55);
  color: #fff;
  font-size: 11px;
  padding: 3px 10px;
  border-radius: 999px;
  backdrop-filter: blur(4px);
}

.status-tag {
  position: absolute;
  top: 10px;
  right: 10px;
}

.info {
  padding: 14px;
}

.title {
  font-size: 14px;
  line-height: 1.5;
  height: 42px;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  color: var(--text);
  font-weight: 500;
}

.meta {
  margin-top: 10px;
  display: flex;
  align-items: baseline;
  gap: 2px;
}

.price {
  color: var(--price);
  font-size: 19px;
  font-weight: 700;
}

.price-origin {
  color: var(--text-3);
  font-size: 12px;
  text-decoration: line-through;
  margin-left: 6px;
}

.bottom {
  margin-top: 8px;
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: var(--text-3);
}

.seller {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid var(--border);
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
  color: var(--text-2);
}

.seller .user-ic {
  color: var(--brand);
}

.seller-name {
  max-width: 90px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.seller .views {
  margin-left: auto;
  color: var(--text-3);
}
</style>
