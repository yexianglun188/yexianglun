# 校园二手交易平台

一个面向高校学生的二手物品交易平台，支持商品发布、搜索筛选、在线下单、收藏留言、站内私信和后台管理。

> 课程实践项目 · Spring Boot 3 + Vue 3 前后端分离架构
>
> 在基础交易流程之上，本次新增三大能力：**站内私信聊天**、**模拟支付流程**（无真实网关）、**管理员数据看板**；并对全部前端页面做了现代化 UI 重构（设计令牌体系 + 响应式布局）。

## 技术栈

| 层次 | 技术选型 |
| --- | --- |
| 后端框架 | Spring Boot 3.3.5、Spring MVC |
| 持久层 | MyBatis-Plus 3.5.9、MySQL 8.0 |
| 认证鉴权 | JWT（jjwt 0.12.6）、自定义注解 + 拦截器 |
| 工具库 | Lombok、Hutool |
| 前端框架 | Vue 3、Vue Router 4、Pinia |
| UI 组件 | Element Plus 2.8 |
| 构建工具 | Maven 3.9、Vite 5 |
| 运行环境 | JDK 17 |

## 功能清单

**普通用户**
- 注册 / 登录（JWT 令牌认证）
- 浏览商品、按分类筛选、关键字搜索、多种排序
- 发布闲置商品（最多 6 张图片上传）
- 商品详情：浏览计数、收藏、公开留言
- 站内私信聊天：与卖家一对一沟通、未读消息提醒、会话列表
- 下单购买 → 模拟支付 → 取消 / 卖家确认 / 买家确认完成
- 个人中心：我发布的 / 我的收藏 / 我买到的 / 我卖出的 / 账号设置

**管理员**
- 数据看板：核心指标（用户数 / 在售 / 已售 / 订单 / GMV）、分类占比、销售趋势图
- 用户管理：查看用户列表、启用 / 禁用账号
- 商品管理：查看全部商品（含各状态）、下架违规商品

## 项目结构

```
campus-market/
├── pom.xml
├── src/main/java/com/campus/market/
│   ├── CampusMarketApplication.java   启动类
│   ├── annotation/                    @RequireLogin / @RequireAdmin 权限注解
│   ├── common/                        统一返回体、状态码、分页结果、全局异常处理
│   ├── config/                        MyBatis-Plus 分页、Web 拦截器与静态资源
│   ├── controller/                    10 个 REST 控制器（含订单/私信/后台）
│   ├── dto/                           请求参数对象（含参数校验注解）
│   ├── entity/                        7 张表的实体类
│   ├── interceptor/                   JWT 鉴权拦截器
│   ├── mapper/                        MyBatis-Plus Mapper 接口
│   ├── service/                       业务接口与实现
│   └── util/                          JWT 工具、登录用户上下文
├── src/main/resources/
│   ├── application.yml                配置文件
│   ├── mapper/*.xml                   MyBatis 联表查询 SQL
│   └── db/schema.sql, data.sql        建表脚本与初始化数据
├── frontend/                          Vue 3 前端工程
│   ├── vite.config.js                 开发代理配置（/api、/uploads → 8080）
│   └── src/
│       ├── api/index.js               接口统一封装与拦截器
│       ├── router/index.js            路由与权限守卫
│       ├── store/user.js              Pinia 用户状态
│       ├── style.css                  设计令牌与设计系统
│       ├── components/ProductCard.vue 商品卡片组件
│       └── views/                     首页 / 登录 / 详情 / 发布 / 个人中心 / 后台 / 私信聊天
├── tools/apache-maven-3.9.9/          内置 Maven（start-backend.bat 使用，零额外安装）
├── start-backend.bat                  一键启动后端
└── start-frontend.bat                 一键启动前端
```

## 快速开始

### 1. 准备环境

- JDK 17
- MySQL 8.0（已启动）
- Node.js 18+

> 后端已内置 Maven（`tools/apache-maven-3.9.9`），无需自行安装 Maven。

### 2. 初始化数据库

```bash
mysql -uroot -p < src/main/resources/db/schema.sql
mysql -uroot -p < src/main/resources/db/data.sql
```

### 3. 修改数据库密码

编辑 `src/main/resources/application.yml`，把 `password` 改成你自己的 MySQL 密码：

```yaml
spring:
  datasource:
    username: root
    password: 你的密码
```

### 4. 启动后端

**方式一（推荐，零安装）**：直接双击项目根目录的 `start-backend.bat`，它会使用内置 Maven 在 8080 端口启动。

**方式二（自定义）**：

```bash
mvn spring-boot:run
```

启动成功后访问：http://localhost:8080

### 5. 启动前端

```bash
cd frontend
npm install
npm run dev
```

打开 http://localhost:5173 即可访问。（也可双击 `start-frontend.bat` 自动完成安装与启动。）

## 测试账号

| 账号 | 密码 | 角色 |
| --- | --- | --- |
| admin | 123456 | 管理员 |
| zhangsan | 123456 | 普通用户 |
| lisi | 123456 | 普通用户 |

## 新增功能说明（本次迭代）

### 1. 站内私信聊天

- 在商品详情页点击「联系卖家」即可发起对话，进入 `/chat` 私信页。
- 会话列表（`GET /api/messages/conversations`）展示最近联系人、最后一条消息与未读数。
- 消息记录（`GET /api/messages?otherId={userId}`）按时间排序；发送（`POST /api/messages`）后自动将对方消息标记为已读。
- 顶部导航实时显示未读总数（`GET /api/messages/unread`）。
- 消息表原 `read` 字段因与 MySQL 保留字冲突，已重命名为 `is_read`（列名 + 实体 `@TableField` 映射同步修改）。

### 2. 模拟支付流程

- 下单（`POST /api/orders`）生成「待支付」订单，商品状态不变。
- 调用 `PUT /api/orders/{id}/pay` 完成**模拟支付**：仅更新订单状态为「待确认」并记录支付时间，**不接入任何真实支付网关**。
- 订单状态机：`0 待支付 → 1 待确认 → 2 交易中 → 3 已完成`，以及 `4 已取消`。支付后由卖家确认、买家确认完成推进，任意一方均可取消。
- 已售商品（`status=2`）不允许删除，从业务层保证数据一致。

### 3. 管理员数据看板

- `GET /api/admin/dashboard` 返回核心指标：用户数、在售/已售商品数、订单数、已完成数、GMV，以及分类占比与销售趋势数据。
- 前端 `/admin` 看板页以指标卡 + 分类占比进度条 + 趋势柱状图呈现，空数据有占位提示。

## 接口一览

统一返回格式：`{ "code": 200, "message": "操作成功", "data": ... }`

> 鉴权失败时不抛 HTTP 401，而是在响应体中返回 `code: 401`，前端拦截器据此跳转登录。

| 模块 | 方法 | 路径 | 说明 |
| --- | --- | --- | --- |
| 认证 | POST | /api/auth/register | 注册 |
| 认证 | POST | /api/auth/login | 登录，返回 token |
| 认证 | GET | /api/auth/me | 当前登录用户 |
| 分类 | GET | /api/categories | 分类列表 |
| 商品 | GET | /api/products | 商品列表（搜索/筛选/排序/分页） |
| 商品 | GET | /api/products/{id} | 商品详情（累加浏览量） |
| 商品 | POST | /api/products | 发布商品 |
| 商品 | PUT | /api/products | 编辑商品 |
| 商品 | PUT | /api/products/{id}/offline | 下架 |
| 商品 | DELETE | /api/products/{id} | 删除 |
| 商品 | GET | /api/products/mine | 我发布的 |
| 订单 | POST | /api/orders | 下单（生成待支付订单） |
| 订单 | PUT | /api/orders/{id}/pay | 模拟支付（订单进入待确认，无真实网关） |
| 订单 | GET | /api/orders | 我的订单（buy / sell） |
| 订单 | PUT | /api/orders/{id}/confirm | 卖家确认交易 |
| 订单 | PUT | /api/orders/{id}/finish | 买家确认完成 |
| 订单 | PUT | /api/orders/{id}/cancel | 取消订单 |
| 收藏 | POST | /api/favorites/toggle/{productId} | 收藏 / 取消收藏 |
| 收藏 | GET | /api/favorites | 我的收藏 |
| 留言 | GET | /api/comments/product/{productId} | 商品留言 |
| 留言 | POST | /api/comments | 发表留言 |
| 私信 | GET | /api/messages/conversations | 会话列表（含未读数） |
| 私信 | GET | /api/messages?otherId={userId} | 与某用户的消息记录 |
| 私信 | POST | /api/messages | 发送私信 |
| 私信 | GET | /api/messages/unread | 当前用户未读消息总数 |
| 文件 | POST | /api/files/upload | 上传图片 |
| 管理 | GET | /api/admin/dashboard | 数据看板（指标 + 分类占比 + 趋势） |
| 管理 | GET | /api/admin/users | 用户列表（管理员） |
| 管理 | PUT | /api/admin/users/{id}/status | 启用 / 禁用用户 |
| 管理 | GET | /api/admin/products | 商品列表（管理员） |
| 管理 | PUT | /api/admin/products/{id}/offline | 下架商品 |

## 界面升级（UI 重构）

本次对前端做了整体视觉重构，建立了一套基于 CSS 变量的设计令牌（Design Token）体系，统一品牌色、圆角、阴影与间距，并覆盖了 Element Plus 的主题变量（`--el-color-primary` 等）。主要变化：

- **全局设计系统**：`frontend/src/style.css` 定义 `:root` 设计令牌，配色以品牌蓝 `#3a6df0` 为主、价格红 `#ff5a5f` 为辅；玻璃拟态吸顶导航、渐变顶栏、统一卡片与按钮样式、自定义滚动条。
- **登录页**：改为左右分栏（左侧品牌展示 + 功能亮点，右侧表单），移动端自动堆叠为单列。
- **首页 / 商品卡片**：渐变 Banner + 光晕装饰；商品卡片 4:3 封面、渐变占位图、分类玻璃标签、悬浮上浮动效、卖家风采行。
- **商品详情**：左侧大图画廊、右侧粘性信息栏与渐变价格条、卖家胶囊、全宽操作按钮。
- **数据看板**：指标卡自动适配网格 + 左侧渐变强调条；分类占比进度条、销售趋势柱状图，空数据有占位提示。
- **私信聊天**：会话列表选中态左侧品牌描边；消息气泡区分己方（微信绿渐变）/ 对方，气泡带缺口细节。
- **个人中心**：渐变侧栏与头像区、菜单选中态、收藏项悬浮、支付提示框。
- 全部页面均做了响应式处理，窄屏自动堆叠。

## 技术要点

**1. JWT 认证与注解式权限控制**

登录成功后签发 JWT（含用户 id 与角色）。拦截器统一解析 Token 写入 `ThreadLocal` 上下文，再根据 Controller 方法或类上的 `@RequireLogin` / `@RequireAdmin` 注解决定是否放行。请求结束后清理 ThreadLocal，避免线程复用导致用户信息串号。

**2. 下单并发控制**

下单时使用条件更新抢占商品：

```sql
UPDATE product SET status = 2 WHERE id = ? AND status = 1
```

只有受影响行数为 1 才继续创建订单，从数据库层面防止同一商品被重复下单。

**3. 计数字段并发安全**

浏览量与收藏数使用 SQL 自增（`view_count = view_count + 1`），避免「先查询再写回」造成的更新丢失。

**4. 统一异常处理**

业务异常、参数校验异常、系统异常由 `@RestControllerAdvice` 统一捕获并转换为标准返回体，Controller 层不出现 try-catch。

**5. 逻辑删除**

商品、用户、订单、留言表使用 `deleted` 字段做逻辑删除，由 MyBatis-Plus 的 `@TableLogic` 自动处理查询过滤。

**6. 图片上传安全**

后端校验扩展名白名单（jpg/png/gif/webp/bmp）与文件大小上限，按日期分目录、UUID 重命名后落盘，防止恶意文件与文件名冲突。

**7. 订单状态机与模拟支付**

订单状态：`0 待支付 → 1 待确认 → 2 交易中 → 3 已完成`，以及 `4 已取消`。下单后进入「待支付」，调用 `PUT /api/orders/{id}/pay` 模拟支付（仅更新状态与支付时间，**不接入任何真实支付网关**），随后由卖家确认、买家确认完成推进，任意一方均可取消。已售商品不允许删除，从业务层保证数据一致。

## 开发说明

- 前端开发环境通过 Vite 代理把 `/api` 与 `/uploads` 转发到后端 8080 端口，无需处理跨域
- 上传的图片保存在项目根目录 `uploads/` 下，通过 `/uploads/**` 访问
- 密码使用 BCrypt 加密存储，实体类上的 `password` 字段标记为只写，不会返回给前端
