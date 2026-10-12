<template>
  <div class="chat-page page-card">
    <div class="chat-layout">
      <!-- 左侧会话列表 -->
      <div class="conv-list">
        <div class="conv-head">私信</div>
        <div v-if="conversations.length" class="conv-items">
          <div
            v-for="c in conversations"
            :key="(c.productId || 'n') + '-' + c.peerId"
            class="conv-item"
            :class="{ active: isActive(c) }"
            @click="openConversation(c)"
          >
            <el-avatar :size="40" :src="c.peerAvatar">{{ (c.peerNickname || '同').charAt(0) }}</el-avatar>
            <div class="conv-info">
              <div class="conv-top">
                <span class="conv-name">{{ c.peerNickname || '匿名同学' }}</span>
                <span class="conv-time">{{ formatTime(c.lastTime) }}</span>
              </div>
              <div class="conv-bottom">
                <span class="conv-last">{{ c.lastContent }}</span>
                <el-badge v-if="c.unread" :value="c.unread" type="danger" class="conv-badge" />
              </div>
              <div v-if="c.productTitle" class="conv-product">📦 {{ c.productTitle }}</div>
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无会话，去商品页给卖家发条消息吧" :image-size="80" />
      </div>

      <!-- 右侧聊天区（手机端全屏覆盖，带返回按钮） -->
      <div class="chat-panel" :class="{ 'has-peer': activePeer }">
        <template v-if="activePeer">
          <div class="chat-header">
            <el-button class="chat-back" :icon="ArrowLeft" circle @click="activePeer = null" />
            <span class="chat-title">{{ peerName }}</span>
            <span v-if="activePeer.productTitle" class="chat-product">📦 {{ activePeer.productTitle }}</span>
          </div>
          <div ref="msgBox" class="msg-box">
            <div v-for="m in messages" :key="m.id" class="msg-row" :class="{ mine: m.fromId === myId }">
              <el-avatar v-if="m.fromId !== myId" :size="34" :src="m.fromAvatar">
                {{ (m.fromNickname || '同').charAt(0) }}
              </el-avatar>
              <div class="bubble">{{ m.content }}</div>
              <el-avatar v-if="m.fromId === myId" :size="34" :src="myAvatar">
                {{ (myName || '我').charAt(0) }}
              </el-avatar>
            </div>
            <el-empty v-if="!messages.length" description="还没有消息，打个招呼吧～" :image-size="70" />
          </div>
          <div class="msg-input">
            <el-input
              v-model="input"
              type="textarea"
              :rows="3"
              maxlength="1000"
              resize="none"
              placeholder="输入消息，回车发送"
              @keyup.enter.exact.prevent="send"
            />
            <el-button type="primary" :disabled="!input.trim()" @click="send">发送</el-button>
          </div>
        </template>
        <el-empty v-else description="选择一个会话开始聊天" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, nextTick, watch } from 'vue'
import { useRoute } from 'vue-router'
import { chatApi } from '../api'
import { useUserStore } from '../store/user'
import { ArrowLeft } from '@element-plus/icons-vue'

const route = useRoute()
const userStore = useUserStore()
const myId = computed(() => userStore.user?.id)
const myName = computed(() => userStore.nickname)
const myAvatar = computed(() => userStore.user?.avatar)

const conversations = ref([])
const activePeer = ref(null)
const messages = ref([])
const input = ref('')
const msgBox = ref(null)
let timer = null

const peerName = computed(() => activePeer.value?.peerNickname || '对方')

function formatTime(t) {
  if (!t) return ''
  const d = new Date(t)
  const now = new Date()
  const pad = n => String(n).padStart(2, '0')
  const hhmm = pad(d.getHours()) + ':' + pad(d.getMinutes())
  if (d.toDateString() === now.toDateString()) return hhmm
  return (d.getMonth() + 1) + '-' + d.getDate() + ' ' + hhmm
}

function isActive(c) {
  return activePeer.value && activePeer.value.peerId === c.peerId && activePeer.value.productId === c.productId
}

async function loadConversations() {
  try {
    const res = await chatApi.conversations()
    conversations.value = res.data || []
  } catch {
    // 忽略
  }
}

async function loadMessages() {
  if (!activePeer.value) return
  try {
    const res = await chatApi.list(activePeer.value.peerId, activePeer.value.productId)
    messages.value = res.data || []
    scrollToBottom()
  } catch {
    // 忽略
  }
}

function openConversation(c) {
  activePeer.value = {
    peerId: c.peerId,
    productId: c.productId,
    peerNickname: c.peerNickname,
    peerAvatar: c.peerAvatar,
    productTitle: c.productTitle
  }
  loadMessages()
}

function openDirect(peerId, productId) {
  activePeer.value = { peerId, productId, peerNickname: '对方', peerAvatar: '', productTitle: '' }
  loadMessages()
}

async function send() {
  if (!input.value.trim() || !activePeer.value) return
  const content = input.value.trim()
  input.value = ''
  try {
    const res = await chatApi.send({
      toId: activePeer.value.peerId,
      productId: activePeer.value.productId,
      content
    })
    messages.value.push(res.data)
    scrollToBottom()
    loadConversations()
  } catch {
    // 错误已由响应拦截器提示
  }
}

function scrollToBottom() {
  nextTick(() => {
    if (msgBox.value) msgBox.value.scrollTop = msgBox.value.scrollHeight
  })
}

async function poll() {
  await loadConversations()
  if (activePeer.value) await loadMessages()
}

function handleQuery() {
  const pid = route.query.peerId ? Number(route.query.peerId) : null
  if (!pid) return
  const prid = route.query.productId ? Number(route.query.productId) : null
  if (activePeer.value && activePeer.value.peerId === pid && activePeer.value.productId === (prid ?? null)) return
  const conv = conversations.value.find(c => c.peerId === pid && c.productId === (prid ?? null))
  if (conv) openConversation(conv)
  else openDirect(pid, prid)
}

onMounted(async () => {
  await loadConversations()
  handleQuery()
  timer = setInterval(poll, 4000)
})

watch(() => route.query.peerId, handleQuery)

onBeforeUnmount(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
.chat-page {
  padding: 0;
  overflow: hidden;
}

.chat-layout {
  display: flex;
  height: calc(100vh - 220px);
  min-height: 480px;
}

.conv-list {
  width: 300px;
  border-right: 1px solid var(--border);
  display: flex;
  flex-direction: column;
  background: var(--surface);
}

.conv-head {
  padding: 18px 16px;
  font-size: 16px;
  font-weight: 700;
  border-bottom: 1px solid var(--border);
  color: var(--text);
}

.conv-items {
  flex: 1;
  overflow-y: auto;
}

.conv-item {
  display: flex;
  gap: 10px;
  padding: 12px 16px;
  cursor: pointer;
  border-bottom: 1px solid #f5f7fa;
  border-left: 3px solid transparent;
  transition: background 0.15s;
}

.conv-item:hover {
  background: var(--surface-2);
}

.conv-item.active {
  background: var(--el-color-primary-light-9);
  border-left-color: var(--brand);
}

.conv-info {
  flex: 1;
  min-width: 0;
}

.conv-top {
  display: flex;
  justify-content: space-between;
}

.conv-name {
  font-weight: 600;
  font-size: 14px;
  color: var(--text);
}

.conv-time {
  font-size: 12px;
  color: var(--text-3);
}

.conv-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 4px;
}

.conv-last {
  color: var(--text-3);
  font-size: 13px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
}

.conv-product {
  margin-top: 4px;
  font-size: 12px;
  color: var(--brand);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.chat-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  background: var(--surface);
}

.chat-header {
  padding: 14px 18px;
  border-bottom: 1px solid var(--border);
  background: var(--surface-2);
  display: flex;
  align-items: center;
  gap: 10px;
}

.chat-title {
  font-weight: 700;
  color: var(--text);
}

.chat-product {
  font-size: 12px;
  color: var(--brand);
}

.msg-box {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background: linear-gradient(180deg, #f4f6fb, #f7f8fa);
}

.msg-row {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
  align-items: flex-start;
}

.msg-row.mine {
  flex-direction: row-reverse;
}

.bubble {
  max-width: 62%;
  padding: 11px 15px;
  border-radius: 14px;
  background: #fff;
  line-height: 1.6;
  word-break: break-word;
  white-space: pre-wrap;
  box-shadow: 0 2px 8px rgba(31, 45, 80, 0.06);
  border-top-left-radius: 4px;
}

.msg-row.mine .bubble {
  background: linear-gradient(135deg, #9be36f, #6fd94f);
  color: #1f3d12;
  border-top-left-radius: 14px;
  border-top-right-radius: 4px;
  box-shadow: 0 2px 8px rgba(110, 200, 80, 0.25);
}

.msg-input {
  padding: 12px;
  border-top: 1px solid var(--border);
  background: var(--surface);
  display: flex;
  gap: 10px;
  align-items: flex-end;
}

.msg-input .el-input {
  flex: 1;
}

/* 返回按钮：仅移动端显示 */
.chat-back {
  display: none;
}

/* ===================== 手机端适配 ===================== */
@media (max-width: 768px) {
  .chat-layout {
    height: calc(100vh - 150px);
    min-height: 0;
  }

  /* 会话列表占满整行 */
  .conv-list {
    width: 100%;
    border-right: none;
  }
  .conv-item {
    padding: 12px 14px;
  }

  /* 选中会话后，聊天窗全屏覆盖 */
  .chat-panel.has-peer {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    z-index: 60;
  }
  .chat-panel:not(.has-peer) {
    display: none;
  }
  .chat-back {
    display: inline-flex;
  }
  .chat-header {
    padding: 10px 12px;
    position: sticky;
    top: 0;
  }

  .msg-box {
    padding: 12px;
  }
  .bubble {
    max-width: 78%;
  }

  .msg-input {
    padding: 8px;
    gap: 8px;
  }
  .msg-input :deep(.el-textarea__inner) {
    min-height: 42px !important;
  }
}
</style>
