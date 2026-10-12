// 端到端冒烟测试：验证 站内私信 / 支付 / 管理看板 三大功能
// 运行：node smoke_test.mjs   （后端需在 http://127.0.0.1:8080 启动）
const BASE = 'http://127.0.0.1:8080/api'

let pass = 0, fail = 0
const results = []
function record(name, ok, detail) {
  if (ok) pass++; else fail++
  results.push(`${ok ? '✅' : '❌'} ${name}${detail ? '  — ' + detail : ''}`)
}

async function call(method, path, { token, body, params } = {}) {
  const url = new URL(BASE + path)
  if (params) for (const [k, v] of Object.entries(params)) if (v !== undefined && v !== null) url.searchParams.set(k, v)
  const headers = { 'Content-Type': 'application/json' }
  if (token) headers['Authorization'] = 'Bearer ' + token
  const res = await fetch(url, { method, headers, body: body ? JSON.stringify(body) : undefined })
  let data = null
  try { data = await res.json() } catch {}
  return { status: res.status, data }
}

const ok = (r) => r && r.data && r.data.code === 200
const dataOf = (r) => (r && r.data ? r.data.data : null)

async function main() {
  // 0) 开放接口：分类列表（无需登录）
  const cats = await call('GET', '/categories')
  record('开放接口 /categories 可访问', ok(cats), `分类数=${Array.isArray(dataOf(cats)) ? dataOf(cats).length : 'n/a'}`)
  const categoryId = Array.isArray(dataOf(cats)) && dataOf(cats).length ? dataOf(cats)[0].id : null

  // 1) 管理员登录 + 看板
  const adminLogin = await call('POST', '/auth/login', { body: { username: 'admin', password: '123456' } })
  record('管理员登录 admin/123456', ok(adminLogin), ok(adminLogin) ? `拿到 token` : JSON.stringify(adminLogin?.data))
  const adminToken = dataOf(adminLogin)?.token
  if (adminToken) {
    const dash = await call('GET', '/admin/dashboard', { token: adminToken })
    const d = dataOf(dash)
    record('管理看板 /admin/dashboard 返回 200', ok(dash) && d, `用户=${d?.userCount} 商品=${d?.productCount} GMV=${d?.totalGmv}`)
    const unread = await call('GET', '/messages/unread', { token: adminToken })
    record('私信未读数 /messages/unread 返回 200', ok(unread), `unread=${dataOf(unread)}`)
    const conv = await call('GET', '/messages/conversations', { token: adminToken })
    record('会话列表 /messages/conversations 返回 200', ok(conv))
  }

  // 2) 支付流程：注册卖家+买家，发布商品，下单并支付（自包含，结束后清理）
  const stamp = Date.now()
  const rnd = Math.floor(Math.random() * 900000) + 100000   // 6 位数字，保证用户名 <=20 字符
  const sellerName = 'smk_s_' + rnd
  const buyerName = 'smk_b_' + rnd
  const regS = await call('POST', '/auth/register', { body: { username: sellerName, password: '123456', nickname: '测试卖家' } })
  const regB = await call('POST', '/auth/register', { body: { username: buyerName, password: '123456', nickname: '测试买家' } })
  record('注册测试卖家/买家', ok(regS) && ok(regB))

  const sLogin = await call('POST', '/auth/login', { body: { username: sellerName, password: '123456' } })
  const bLogin = await call('POST', '/auth/login', { body: { username: buyerName, password: '123456' } })
  const sellerToken = dataOf(sLogin)?.token
  const buyerToken = dataOf(bLogin)?.token
  record('测试账号登录获取 token', !!sellerToken && !!buyerToken)

  let productId = null, orderId = null
  if (sellerToken && categoryId) {
    const pub = await call('POST', '/products', {
      token: sellerToken,
      body: {
        title: '冒烟测试商品-' + stamp,
        categoryId,
        price: 9.9,
        originalPrice: 19.9,
        itemCondition: '几乎全新',
        tradePlace: '测试地点',
        description: '自动化冒烟测试创建',
        coverImage: '',
        images: []
      }
    })
    productId = dataOf(pub)
    record('卖家发布商品', ok(pub), `productId=${productId}`)
  }

  if (buyerToken && productId) {
    const ord = await call('POST', '/orders', { token: buyerToken, body: { productId, contact: '13800000000', remark: '冒烟测试订单' } })
    orderId = dataOf(ord)
    record('买家创建订单（待支付）', ok(ord), `orderId=${orderId}`)

    // 支付前商品应为在售(1)
    const before = await call('GET', `/products/${productId}`)
    const beforeStatus = dataOf(before)?.status
    const pay = await call('PUT', `/orders/${orderId}/pay`, { token: buyerToken })
    record('买家支付订单', ok(pay))

    // 支付后商品应为已售(2)，订单应为待确认(1)
    const after = await call('GET', `/products/${productId}`)
    const afterStatus = dataOf(after)?.status
    record('支付后商品变为已售(2)', afterStatus === 2, `status: ${beforeStatus} → ${afterStatus}`)

    const orders = await call('GET', '/orders', { token: buyerToken, params: { role: 'buy', status: 1, pageNum: 1, pageSize: 10 } })
    const paidOrder = (dataOf(orders)?.records || []).find(o => o.id === orderId)
    record('支付后订单状态为待确认(1)', paidOrder?.status === 1, `orderStatus=${paidOrder?.status}`)
  }

  // 3) 鉴权：未登录访问支付接口应被拦截（HTTP 401 或响应体 code=401）
  const noAuth = await call('PUT', orderId ? `/orders/${orderId}/pay` : '/orders/999999/pay')
  const blocked = noAuth.status === 401 || noAuth.data?.code === 401
  record('未登录访问支付接口被拦截', blocked, `HTTP ${noAuth.status} code=${noAuth.data?.code}`)

  // 4) 私信：买家给卖家发一条消息
  if (buyerToken && sellerToken && productId) {
    const sellerId = dataOf(sLogin)?.user?.id
    const send = await call('POST', '/messages', { token: buyerToken, body: { toId: sellerId, productId, content: '你好，这个还在吗？' } })
    record('买家发送私信', ok(send), `msgId=${dataOf(send)?.id}`)
    const conv2 = await call('GET', '/messages/conversations', { token: sellerToken })
    const found = (dataOf(conv2) || []).some(c => c.peerId === dataOf(bLogin)?.user?.id && c.productId === productId)
    record('卖家会话列表可见该会话', found)
  }

  // 5) 清理：删除测试商品（卖家）
  if (sellerToken && productId) {
    const del = await call('DELETE', `/products/${productId}`, { token: sellerToken })
    record('清理：删除测试商品', ok(del))
  }

  console.log('\n================ 冒烟测试结果 ================')
  for (const r of results) console.log(r)
  console.log(`\n通过 ${pass} / 失败 ${fail}`)
  process.exit(fail === 0 ? 0 : 1)
}

main().catch(e => { console.error('测试异常:', e); process.exit(2) })
