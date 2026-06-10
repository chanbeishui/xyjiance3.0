# 集信管理系统 (xyjiance)

基于 Spring Boot + Vue3 前后端分离的**集团企业管理框架**，支持 Web 后台 + 微信小程序双端。

## 技术栈

| 层级 | 技术 | 版本 |
|------|------|------|
| **后端框架** | Spring Boot | 4.x |
| **JDK** | Java | 17+ |
| **构建工具** | Maven | 3.9+ |
| **安全框架** | Spring Security + JWT | 无状态认证 |
| **ORM** | MyBatis | 3.x |
| **数据库** | MySQL | 8.0+ |
| **连接池** | Druid | 1.2 |
| **缓存** | Redis | 6.x+ |
| **分页** | PageHelper | 2.1 |
| **API文档** | SpringDoc (OpenAPI 3.0) | 3.0 |
| **定时任务** | Quartz | 2.x |
| **代码生成** | Velocity 模板引擎 | 2.3 |
| **Web前端** | Vue 3 + Vite + Element Plus + Pinia | Vue Router 4 |
| **小程序** | uni-app (Vue 3) + uView Plus | 微信小程序 |

## 项目结构

```
xyjiance3.0/
├── xyjiance-admin/       # 【后端入口】Spring Boot 启动类 + 所有 Controller
│   └── src/main/java/com/xyjiance/
│       ├── XyJianceApplication.java      # 启动入口
│       └── web/controller/
│           ├── system/                   # 系统管理（用户/角色/菜单/部门/公司）
│           ├── monitor/                  # 监控管理（日志/在线用户/服务监控）
│           └── common/                   # 通用接口（文件上传/验证码）
│
├── xyjiance-common/      # 【通用模块】实体类、工具类、注解、异常、枚举
│   └── src/main/java/com/xyjiance/common/
│       ├── core/domain/entity/           # 核心实体（SysUser, SysCompany, SysDept, SysRole, SysMenu）
│       ├── core/domain/model/            # 请求/响应模型（LoginUser, LoginBody, WxLoginBody）
│       ├── annotation/                  # 注解（@Log, @DataScope, @Anonymous, @RateLimiter 等）
│       └── utils/                       # 工具类（String, Date, Security, Excel 等）
│
├── xyjiance-framework/   # 【框架层】Spring Security 配置、JWT 认证、AOP 切面、Token 服务
│   └── src/main/java/com/xyjiance/framework/
│       ├── config/                      # 配置类（Security, CORS, Redis, Druid, MyBatis）
│       ├── security/                    # JWT 过滤器、认证入口、权限上下文
│       ├── aspectj/                     # AOP 切面（@Log, @DataScope, @DataSource, @RateLimiter）
│       ├── web/service/                 # Token 服务、登录服务、微信登录服务
│       └── datasource/                  # 多数据源动态切换
│
├── xyjiance-system/      # 【系统业务】Service + Mapper
│   └── src/main/java/com/xyjiance/system/
│       ├── service/                     # 业务接口和实现（用户/角色/公司/部门/菜单/字典等）
│       └── mapper/                      # MyBatis Mapper 接口
│   └── src/main/resources/mapper/system/  # MyBatis XML 映射文件
│
├── xyjiance-generator/   # 【代码生成器】Velocity 模板，读取数据库表自动生成 CRUD
├── xyjiance-quartz/      # 【定时任务】Quartz 任务调度模块
├── xyjiance-ui/          # 【Web 前端】Vue 3 + Vite + Element Plus
├── xyjiance-app/         # 【小程序端】uni-app (Vue 3) + uView Plus
├── sql/                  # 数据库脚本
│   ├── ry_20260417.sql           # 基础建表脚本
│   ├── ry_20260608_company.sql   # 集团架构迁移脚本
│   └── ry_20260608_wx.sql        # 微信小程序迁移脚本
└── pom.xml               # 根 POM（Maven 多模块）
```

## 核心架构设计

### 1. 集团组织架构（公司 → 部门 → 用户）

```
集信集团(总公司)
├── A子公司
│   ├── 研发部 ── 用户
│   └── 销售部 ── 用户
├── B子公司
│   ├── 研发部 ── 用户
│   └── 财务部 ── 用户
└── C分公司
    └── 运维部 ── 用户
```

- `sys_company` — 公司表，树形结构（parentId + ancestors），支持集团-子公司多级
- `sys_dept` — 部门表，新增 `company_id` 关联所属公司，每个子公司的部门独立
- `sys_user` — 用户表，新增 `company_id` 主属公司，`dept_id` 所属部门

### 2. 认证与权限

```
小程序/浏览器
    │
    ├─ POST /login        → 账号密码登录 → 返回 JWT token
    ├─ POST /wx/login      → 微信 code 登录 → 返回 JWT token
    │
    └─ API 请求（Header: Authorization: Bearer <token>)
            │
            ├─ JwtAuthenticationTokenFilter  从 Header 解析 token
            ├─ TokenService                  从 Redis 获取 LoginUser
            ├─ Spring Security               @PreAuthorize 按钮级权限
            └─ @DataScope                    数据权限（SQL 注入过滤条件）
```

**七级数据权限：**

| 级别 | 常量 | 含义 |
|------|------|------|
| 1 | DATA_SCOPE_ALL | 全部数据（管理员） |
| 2 | DATA_SCOPE_CUSTOM | 自定义部门（角色绑定部门列表） |
| 3 | DATA_SCOPE_DEPT | 本部门数据 |
| 4 | DATA_SCOPE_DEPT_AND_CHILD | 本部门及子部门 |
| 5 | DATA_SCOPE_SELF | 仅本人 |
| 6 | DATA_SCOPE_COMPANY | **本公司全部数据（集团新增）** |
| 7 | DATA_SCOPE_COMPANY_AND_CHILD | **本公司及下属公司数据（集团新增）** |

### 3. 前端路由机制

- **Web 端：** `constantRoutes`（登录/404/注册，无需权限）+ 动态路由（后端 `/getRouters` 返回菜单树，前端动态注册）
- **小程序端：** 不使用 Vue Router，通过 `pages.json` 配置 TabBar，权限通过返回的功能列表控制页面和按钮可见性

### 4. 微信小程序登录流程

```
小程序端                          后端                       微信服务器
  │                               │                           │
  ├─  wx.login()                  │                           │
  │   └─────────────────────────→ │                           │
  │       POST /wx/login {code}   │                           │
  │                               ├─  code2session ──────────→│
  │                               │←── openId + unionId ──────┤
  │                               ├─ 查 sys_user.wx_open_id   │
  │                               │   已绑定：生成 JWT token    │
  │                               │   未绑定：返回 601 状态码  │
  │←── token / 601 ───────────────┤                           │
  │                               │                           │
```

## 快速启动

### 环境要求

- JDK 17+
- Maven 3.9+
- MySQL 8.0+
- Redis 6.x+
- Node.js 18+

### 后端

```bash
# 1. 创建数据库并导入脚本
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS ry_vue DEFAULT CHARSET utf8mb4"
mysql -u root -p ry_vue < sql/ry_20260417.sql
mysql -u root -p ry_vue < sql/ry_20260608_company.sql
mysql -u root -p ry_vue < sql/ry_20260608_wx.sql

# 2. 修改 application-druid.yml 中的数据库连接信息

# 3. 启动
mvn clean compile -DskipTests
# 运行 xyjiance-admin 中的 XyJianceApplication.java 的 main 方法
# 或：mvn spring-boot:run -pl xyjiance-admin
```

后端默认端口 `8080`，Druid 监控 `/druid/`，API 文档 `/swagger-ui.html`

### Web 前端

```bash
cd xyjiance-ui
npm install
npm run dev          # 开发模式，端口 80，代理 API 到 8080
npm run build:prod   # 生产构建
```

### 微信小程序

```bash
cd xyjiance-app
npm install
npm run dev:mp-weixin   # 编译为微信小程序
# 用微信开发者工具打开 dist/dev/mp-weixin/
```

修改 `src/manifest.json` 中的 `mp-weixin.appid` 为实际小程序 AppID。

## 开发注意事项

### 后端开发

1. **数据权限**：使用 `@DataScope` 注解自动注入部门/公司过滤条件。方法的入参必须是 `BaseEntity` 子类，并在 Mapper XML 的 SQL 末尾引用 `${params.dataScope}`
2. **权限控制**：Controller 方法用 `@PreAuthorize("@ss.hasPermi('权限标识')")`，对应 `sys_menu` 表的 `perms` 字段
3. **匿名访问**：用 `@Anonymous` 注解标记不需认证的接口（如微信登录）
4. **操作日志**：用 `@Log(title, businessType)` 记录关键操作
5. **代码生成**：修改 `xyjiance-generator` 的 `generator.yml` 配置后，访问代码生成页面，基于数据库表自动生成前后端 CRUD 代码

### 集团架构开发注意

1. **新增实体必须关联公司**：所有业务实体表都需要 `company_id` 字段来支持公司级数据隔离
2. **SQL 查询必须带 dataScope**：Mapper XML 中的查询语句末尾必须引用 `${params.dataScope}`，否则数据权限不生效
3. **公司树独立于部门树**：`sys_company` 和 `sys_dept` 是两棵独立的树，部门通过 `company_id` 关联公司
4. **跨公司查询**：集团管理员可配置「本公司及下属公司」权限（dataScope=7）实现跨子公司数据汇总
5. **用户主属公司**：用户有 `company_id`（主属公司）和 `dept_id`（部门），默认按公司隔离数据

### 前端开发注意

1. **环境变量**：使用 `VITE_` 前缀（如 `VITE_APP_BASE_API`），配置在 `.env.*` 文件中
2. **权限指令**：`v-hasPermi` 和 `v-hasRole` 控制按钮/元素可见性
3. **请求封装**：`@/utils/request.js` 统一处理 token 携带和过期跳转
4. **工具函数**：`@/utils/xyjiance.js` 提供日期格式化、字典回显、树形构造等通用函数
5. **新增页面路由**：
   - **Web 端**：在 `sys_menu` 表添加菜单记录（组件路径指向 `@/views/` 下的 `.vue` 文件），角色分配菜单权限后前端动态加载
   - **小程序端**：在 `src/pages.json` 的 `pages` 数组中注册页面路径，TabBar 页面在 `tabBar.list` 中配置

### 小程序开发注意

1. **无 Router**：uni-app 小程序不使用 Vue Router，页面跳转用 `uni.navigateTo` / `uni.switchTab` / `uni.reLaunch`
2. **Token 存储**：使用 `uni.setStorageSync`（替代 Web 端的 js-cookie）
3. **请求封装**：使用 `uni.request`（替代 axios），拦截器通过 `uni.addInterceptor` 实现
4. **微信登录**：首次登录需在前端引导用户绑定已有系统账号（返回 601 状态码时）
5. **组件兼容**：Element Plus 不可用于小程序端，必须使用 uView Plus 或 uni-ui 组件
6. **页面生命周期**：使用 `onLoad`/`onShow`/`onReady`/`onHide`（替代 Vue 的 `mounted`/`created`）
7. **条件编译**：若需跨平台差异化代码，使用 `#ifdef MP-WEIXIN` / `#endif` 注释

### 跨端通用注意

- **API 接口统一**：Web 端和小程序端调用同一套后端 API，认证方式均为 `Authorization: Bearer <token>`
- **响应格式**：统一 `{ code: 200, msg: "...", data: ... }` 或分页 `{ code: 200, msg: "...", rows: [...], total: N }`
- **字典缓存**：前端取 `sys_dict_data` 做本地缓存，小程序端建议持久化到 `uni.setStorage`
- **文件上传**：Web 端用 `FormData`，小程序端用 `uni.uploadFile`，后端统一接收 `multipart/form-data`

## 常用注解一览

| 注解 | 位置 | 作用 |
|------|------|------|
| `@Anonymous` | ruoyi-common | 标记接口可匿名访问（无需 token） |
| `@Log` | ruoyi-common | 操作日志记录，配合 AOP 切面 |
| `@DataScope` | ruoyi-common | 数据权限过滤，注入 SQL 条件到 Mapper XML |
| `@DataSource` | ruoyi-common | 切换数据源（主/从） |
| `@RateLimiter` | ruoyi-common | 接口限流 |
| `@RepeatSubmit` | ruoyi-common | 防止重复提交 |
| `@Sensitive` | ruoyi-common | 数据脱敏（字段级） |
| `@Excel` / `@Excels` | ruoyi-common | Excel 导入导出列映射 |
| `@PreAuthorize("@ss.hasPermi('...')")` | Spring Security | 按钮级权限控制 |

## 配置文件说明

| 文件 | 说明 |
|------|------|
| `xyjiance-admin/src/main/resources/application.yml` | 主配置：端口、Redis、token、MyBatis、SpringDoc、XSS |
| `xyjiance-admin/src/main/resources/application-druid.yml` | 数据源：主从库、Druid 连接池、监控页面 |
| `xyjiance-admin/src/main/resources/logback.xml` | 日志配置：路径、级别、滚动策略 |
| `xyjiance-generator/src/main/resources/generator.yml` | 代码生成器：作者、包路径、表前缀 |
| `xyjiance-ui/.env.development` | 前端开发环境变量 |
| `xyjiance-ui/.env.production` | 前端生产环境变量 |
| `xyjiance-ui/vite.config.js` | Vite 构建配置 + API 代理 |
| `xyjiance-app/vite.config.js` | uni-app 构建配置 + API 代理 |
| `xyjiance-app/src/manifest.json` | 微信小程序 AppID 配置 |
| `xyjiance-app/src/pages.json` | 小程序路由 + TabBar 配置 |

## 数据库表说明

| 表名 | 说明 | 关键字段 |
|------|------|---------|
| `sys_company` | **公司表（新增）** | company_id, parent_id, ancestors |
| `sys_dept` | 部门表 | dept_id, company_id, parent_id |
| `sys_user` | 用户表 | user_id, company_id, dept_id, wx_open_id, wx_union_id |
| `sys_role` | 角色表 | role_id, role_key, data_scope(1-7) |
| `sys_menu` | 菜单权限表 | menu_id, perms, menu_type(M目录/C菜单/F按钮) |
| `sys_role_dept` | 角色-部门关联 | role_id, dept_id（自定义数据权限用） |
| `sys_user_role` | 用户-角色关联 | user_id, role_id |
| `sys_dict_data` / `sys_dict_type` | 字典数据/类型 | — |
| `sys_config` | 系统参数表 | config_key, config_value |
| `sys_notice` | 通知公告表 | — |
| `sys_logininfor` | 登录日志 | — |
| `sys_oper_log` | 操作日志 | — |
| `sys_post` | 岗位表 | — |
| `sys_job` / `sys_job_log` | 定时任务/日志 | quartz 模块 |
