<template>
  <div class="page-card publish-card">
    <div class="section-title">{{ isEdit ? '编辑商品' : '发布闲置' }}</div>

    <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
      <el-form-item label="商品标题" prop="title">
        <el-input v-model="form.title" maxlength="100" show-word-limit placeholder="例如：九成新机械键盘 87键 青轴" />
      </el-form-item>

      <el-form-item label="商品分类" prop="categoryId">
        <el-select v-model="form.categoryId" placeholder="请选择分类" style="width: 220px">
          <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
        </el-select>
      </el-form-item>

      <el-form-item label="出售价格" prop="price">
        <el-input-number v-model="form.price" :min="0.01" :precision="2" :step="1" />
        <span class="unit">元</span>
      </el-form-item>

      <el-form-item label="原价">
        <el-input-number v-model="form.originalPrice" :min="0" :precision="2" :step="1" />
        <span class="unit">元（选填，便于买家对比）</span>
      </el-form-item>

      <el-form-item label="成色">
        <el-radio-group v-model="form.itemCondition">
          <el-radio-button value="全新">全新</el-radio-button>
          <el-radio-button value="几乎全新">几乎全新</el-radio-button>
          <el-radio-button value="轻微使用">轻微使用</el-radio-button>
          <el-radio-button value="明显使用">明显使用</el-radio-button>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="交易地点">
        <el-input v-model="form.tradePlace" maxlength="100" placeholder="例如：东校区图书馆一楼" style="width: 320px" />
      </el-form-item>

      <el-form-item label="商品图片">
        <el-upload
          v-model:file-list="fileList"
          list-type="picture-card"
          :http-request="handleUpload"
          :on-remove="handleRemove"
          :limit="6"
          accept="image/*"
        >
          <el-icon><Plus /></el-icon>
        </el-upload>
        <div class="upload-tip">最多 6 张，第一张作为封面，单张不超过 10MB</div>
      </el-form-item>

      <el-form-item label="商品描述">
        <el-input
          v-model="form.description"
          type="textarea"
          :rows="5"
          maxlength="2000"
          show-word-limit
          placeholder="说说购买时间、使用情况、有无瑕疵，写得越清楚越容易卖出去～"
        />
      </el-form-item>

      <el-form-item>
        <el-button type="primary" :loading="submitting" @click="submit">
          {{ isEdit ? '保存修改' : '立即发布' }}
        </el-button>
        <el-button @click="$router.back()">取消</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { categoryApi, fileApi, productApi, compressImage } from '../api'

const route = useRoute()
const router = useRouter()

const formRef = ref()
const submitting = ref(false)
const categories = ref([])
const fileList = ref([])

const isEdit = computed(() => !!route.params.id)

const form = reactive({
  id: null,
  title: '',
  categoryId: null,
  price: 1,
  originalPrice: null,
  itemCondition: '几乎全新',
  tradePlace: '',
  description: '',
  images: []
})

const rules = {
  title: [{ required: true, message: '请输入商品标题', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择商品分类', trigger: 'change' }],
  price: [{ required: true, message: '请输入出售价格', trigger: 'blur' }]
}

/** 自定义上传：先压缩再走统一封装的 axios，自动携带 token */
async function handleUpload(options) {
  try {
    const compressed = await compressImage(options.file)
    const res = await fileApi.upload(compressed)
    const url = res.data
    form.images.push(url)
    options.onSuccess(res)
  } catch (e) {
    options.onError(e)
  }
}

function handleRemove(file) {
  const url = file.url || file.response?.data
  form.images = form.images.filter(u => u !== url)
}

async function loadDetail() {
  const res = await productApi.detail(route.params.id)
  const p = res.data
  Object.assign(form, {
    id: p.id,
    title: p.title,
    categoryId: p.categoryId,
    price: Number(p.price),
    originalPrice: p.originalPrice ? Number(p.originalPrice) : null,
    itemCondition: p.itemCondition || '几乎全新',
    tradePlace: p.tradePlace,
    description: p.description,
    images: p.imageList || []
  })
  fileList.value = form.images.map((url, i) => ({ name: `图片${i + 1}`, url }))
}

async function submit() {
  await formRef.value.validate()
  submitting.value = true
  try {
    const payload = {
      id: form.id,
      title: form.title,
      categoryId: form.categoryId,
      price: form.price,
      originalPrice: form.originalPrice,
      itemCondition: form.itemCondition,
      tradePlace: form.tradePlace,
      description: form.description,
      coverImage: form.images[0] || null,
      images: form.images
    }
    if (isEdit.value) {
      await productApi.update(payload)
      ElMessage.success('修改成功')
      router.push(`/product/${form.id}`)
    } else {
      const res = await productApi.publish(payload)
      ElMessage.success('发布成功')
      router.push(`/product/${res.data}`)
    }
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  const res = await categoryApi.list()
  categories.value = res.data
  if (isEdit.value) {
    loadDetail()
  }
})
</script>

<style scoped>
.publish-card {
  max-width: 760px;
  margin: 0 auto;
}

.unit {
  margin-left: 10px;
  font-size: 13px;
  color: #909399;
}

.upload-tip {
  font-size: 12px;
  color: #909399;
  line-height: 1.6;
  margin-top: 4px;
}
</style>
