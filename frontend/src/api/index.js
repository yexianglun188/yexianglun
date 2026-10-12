import axios from 'axios'
import { ElMessage } from 'element-plus'

/** 401 未登录时的处理函数，由 main.js 注入，避免 api 层直接依赖 router 造成循环引用 */
let onUnauthorized = null

export function setUnauthorizedHandler(fn) {
  onUnauthorized = fn
}

const request = axios.create({
  baseURL: '/api',
  timeout: 15000
})

// 请求拦截：自动携带 token
request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => Promise.reject(error)
)

// 响应拦截：统一处理业务错误
request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      if (res.code === 401) {
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        if (typeof onUnauthorized === 'function') {
          onUnauthorized()
        }
      }
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res
  },
  error => {
    ElMessage.error('网络异常，请稍后重试')
    return Promise.reject(error)
  }
)

export const authApi = {
  login: data => request.post('/auth/login', data),
  register: data => request.post('/auth/register', data),
  me: () => request.get('/auth/me')
}

export const userApi = {
  detail: id => request.get(`/user/${id}`),
  updateProfile: data => request.put('/user/profile', data),
  changePassword: data => request.put('/user/password', data)
}

export const categoryApi = {
  list: () => request.get('/categories')
}

export const productApi = {
  list: params => request.get('/products', { params }),
  mine: params => request.get('/products/mine', { params }),
  detail: id => request.get(`/products/${id}`),
  publish: data => request.post('/products', data),
  update: data => request.put('/products', data),
  offline: id => request.put(`/products/${id}/offline`),
  online: id => request.put(`/products/${id}/online`),
  remove: id => request.delete(`/products/${id}`)
}

export const orderApi = {
  create: data => request.post('/orders', data),
  list: params => request.get('/orders', { params }),
  confirm: id => request.put(`/orders/${id}/confirm`),
  finish: id => request.put(`/orders/${id}/finish`),
  cancel: id => request.put(`/orders/${id}/cancel`),
  pay: id => request.put(`/orders/${id}/pay`)
}

export const favoriteApi = {
  toggle: productId => request.post(`/favorites/toggle/${productId}`),
  list: params => request.get('/favorites', { params })
}

export const commentApi = {
  listByProduct: productId => request.get(`/comments/product/${productId}`),
  add: data => request.post('/comments', data),
  remove: id => request.delete(`/comments/${id}`)
}

/** 上传前压缩图片：手机照片动辄 3-8MB，隧道上行慢易超时；最长边压到 1600px、JPEG 0.82（GIF 不压缩） */
export function compressImage(file, maxDim = 1600, quality = 0.82) {
  return new Promise(resolve => {
    if (!file.type || !file.type.startsWith('image/') || file.type === 'image/gif') return resolve(file)
    const url = URL.createObjectURL(file)
    const img = new Image()
    img.onload = () => {
      URL.revokeObjectURL(url)
      const scale = Math.min(1, maxDim / Math.max(img.width, img.height))
      if (scale >= 1 && file.size < 500 * 1024) return resolve(file)
      const canvas = document.createElement('canvas')
      canvas.width = Math.max(1, Math.round(img.width * scale))
      canvas.height = Math.max(1, Math.round(img.height * scale))
      canvas.getContext('2d').drawImage(img, 0, 0, canvas.width, canvas.height)
      canvas.toBlob(
        blob =>
          resolve(blob ? new File([blob], file.name.replace(/\.\w+$/, '') + '.jpg', { type: 'image/jpeg' }) : file),
        'image/jpeg',
        quality
      )
    }
    img.onerror = () => {
      URL.revokeObjectURL(url)
      resolve(file)
    }
    img.src = url
  })
}

export const fileApi = {
  upload: (file, { timeout = 60000 } = {}) => {
    const formData = new FormData()
    formData.append('file', file)
    return request.post('/files/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      timeout
    })
  }
}

export const adminApi = {
  users: params => request.get('/admin/users', { params }),
  updateUserStatus: (id, status) =>
    request.put(`/admin/users/${id}/status`, null, { params: { status } }),
  products: params => request.get('/admin/products', { params }),
  offlineProduct: id => request.put(`/admin/products/${id}/offline`),
  dashboard: () => request.get('/admin/dashboard')
}

export const chatApi = {
  conversations: () => request.get('/messages/conversations'),
  list: (peerId, productId) =>
    request.get('/messages', { params: { peerId, productId: productId ?? undefined } }),
  send: data => request.post('/messages', data),
  unread: () => request.get('/messages/unread')
}

export default request
