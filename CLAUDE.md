# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概览

集信（xyjiance）— 集团企业管理框架，Spring Boot + Vue3 前后端分离 + 微信小程序三端架构。

## 环境要求

- JDK 17+ | Maven 3.9+ | MySQL 8.0+ | Redis 6.x+ | Node.js 18+

## Build & Run Commands

**后端 (Spring Boot 4.0.x, JDK 17+, Maven):**
- 编译: `mvn clean compile -DskipTests`
- 启动: 运行 `xyjiance-admin/src/main/java/com/xyjiance/XyJianceApplication.java` 的 main 方法，或 `mvn spring-boot:run -pl xyjiance-admin`
- 打包: `mvn clean package -DskipTests`
- 端口: `8080`
- 注意: 项目目前无自动化测试，`-DskipTests` 为必须参数

**Web 前端 (Vue 3 + Vite + Element Plus) — `xyjiance-ui/`:**
- 安装: `npm install`
- 开发: `npm run dev`（端口 80，代理 `/dev-api` → `localhost:8080`）
- 构建: `npm run build:prod` / `npm run build:stage`
- 预览: `npm run preview`

**小程序 (uniapp Vue 3 + uView Plus) — `xyjiance-app/`:**
- 安装: `npm install`
- 微信开发: `npm run dev:mp-weixin`（用微信开发者工具打开 `dist/dev/mp-weixin/`）
- 微信构建: `npm run build:mp-weixin`
- H5 开发: `npm run dev:h5` / H5 构建: `npm run build:h5`
- App 开发: `npm run dev:app` / App 构建: `npm run build:app`

**数据库:**
- 脚本: `sql/ry_20260417.sql`（主库）、`sql/ry_20260608_company.sql`（集团架构）、`sql/ry_20260608_wx.sql`（微信）、`sql/quartz.sql`（定时任务表）
- 按顺序导入: 主库 → company → wx → quartz
- 连接信息: `application-druid.yml`，数据库名 `ry-vue`

---

## 项目结构（三端）

```
xyjiance3.0/
├── xyjiance-admin/       # 后端入口 — Controller 层
├── xyjiance-common/      # 通用模块 — 实体、注解、工具类、异常、枚举
├── xyjiance-framework/   # 框架层 — Security、JWT、AOP、Token、数据源
├── xyjiance-system/      # 业务层 — Service + Mapper
├── xyjiance-generator/   # 代码生成器
├── xyjiance-quartz/      # 定时任务
├── xyjiance-ui/          # Web 管理后台（Vue3 + Element Plus + Pinia）
├── xyjiance-app/         # 移动端小程序（uniapp Vue3 + uView Plus）
├── sql/                  # 数据库脚本
```

分层: **Controller → Service(IService/ServiceImpl) → Mapper**
- Controller: `xyjiance-admin/web/controller/{system,monitor,common,tool}/`
- Service + Mapper: `xyjiance-system`
- 实体: `xyjiance-common/core/domain/entity/`
- 请求/响应模型: `xyjiance-common/core/domain/model/`
- 响应类: `AjaxResult`（操作响应）、`TableDataInfo`（分页）、`R`（返回值）

---

## 集团组织架构（核心约束）

```
SysCompany（公司树）── 1:N ── SysDept（部门树）── 1:N ── SysUser（用户）
```

- **`SysCompany`**: `companyId`, `parentId`, `ancestors`（树形，集团 → 子公司 → 分公司）
- **`SysDept`**: `companyId` 关联所属公司，每个子公司的部门树完全独立
- **`SysUser`**: `companyId`（主属公司）+ `deptId`（所属部门），`wxOpenId`/`wxUnionId`（微信绑定）
- **数据权限**（`@DataScope`）支持 7 级: 1=全部 2=自定义部门 3=本部门 4=本部门及子部门 5=仅本人 **6=本公司 7=本公司及下属公司**

### ⚠️ 新增业务实体必须遵守的规则

1. **所有业务表加 `company_id` 字段**，否则 `@DataScope` 的公司级过滤对该表无效
2. **Mapper XML 的查询 SQL 末尾引用 `${params.dataScope}`**，这是数据权限注入点
3. **Controller 方法参数须为 `BaseEntity` 子类**，`@DataScope` 才能注入过滤条件
4. **公司树变更**时调用 `SysCompanyServiceImpl.updateCompanyChildren()` 递归更新子节点的 `ancestors`

---

## 认证与权限

**认证流程:**
- 账号密码 → `POST /login` → JWT token（Redis 存储，30分钟过期，近 20 分钟自动续期）
- 微信登录 → `POST /wx/login`（匿名）→ code 换 openId → 查绑定用户 → JWT token
- 所有 API 请求 Header: `Authorization: Bearer <token>`
- `JwtAuthenticationTokenFilter` 解析 token → Redis 查 `LoginUser` → 注入 SecurityContext
- `@Anonymous` 标记的接口跳过认证（如 `/wx/login`、`/captchaImage`）

**权限控制:**
- 按钮级: `@PreAuthorize("@ss.hasPermi('system:user:list')")` 对应 `sys_menu.perms` 字段
- 数据级: `@DataScope(deptAlias = "d", userAlias = "u")` 自动注入 SQL 过滤条件
- 前端: `v-hasPermi="['system:user:list']"` / `v-hasRole="['admin']"` 控制元素显隐

**SecurityConfig 匿名端点**（`xyjiance-framework/.../config/SecurityConfig.java`）:
- `/login`, `/register`, `/captchaImage` — 账号登录
- `/wx/**` — 微信小程序接口
- 静态资源、Swagger、Druid 监控

**`@Anonymous` 工作原理**: `PermitAllUrlProperties` 在启动时扫描所有 Controller 的 `@RequestMapping`，发现有 `@Anonymous` 注解的方法/类时，自动将其 URL 路径（`{pathVariable}` 替换为 `*`）注册到 SecurityConfig 白名单。新增匿名接口只需加 `@Anonymous` 注解，无需修改 SecurityConfig。

---

## 后端开发模式

### 新增一个业务模块的标准流程

1. **数据库建表** — 必须有 `company_id bigint(20) DEFAULT NULL COMMENT '所属公司ID'`
2. **创建实体** — 放在 `xyjiance-common/core/domain/entity/`，继承 `BaseEntity`
3. **创建 Mapper** — 接口在 `xyjiance-system/mapper/`，XML 在 `xyjiance-system/src/main/resources/mapper/`
4. **创建 Service** — 接口 `ISysXxxService` + 实现 `SysXxxServiceImpl`，放 `xyjiance-system/service/`
5. **创建 Controller** — 放 `xyjiance-admin/web/controller/` 对应子包
6. **菜单配置** — 在 `sys_menu` 表插入菜单记录

### Mapper XML 模板

```xml
<select id="selectXxxList" parameterType="Xxx" resultMap="XxxResult">
    <include refid="selectXxxVo"/>
    <where>
        <!-- 业务条件 -->
        ${params.dataScope}   <!-- 必须引用，DataScope 在此注入 -->
    </where>
</select>
```

### Controller 模板

```java
@RestController
@RequestMapping("/system/xxx")
public class SysXxxController extends BaseController {
    @Autowired
    private ISysXxxService xxxService;

    @PreAuthorize("@ss.hasPermi('system:xxx:list')")
    @GetMapping("/list")
    public TableDataInfo list(Xxx xxx) {   // 参数须为 BaseEntity 子类
        startPage();
        List<Xxx> list = xxxService.selectXxxList(xxx);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('system:xxx:add')")
    @Log(title = "XXX管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody Xxx xxx) { ... }

    @PreAuthorize("@ss.hasPermi('system:xxx:edit')")
    @Log(title = "XXX管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody Xxx xxx) { ... }

    @PreAuthorize("@ss.hasPermi('system:xxx:remove')")
    @Log(title = "XXX管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{xxxIds}")
    public AjaxResult remove(@PathVariable Long[] xxxIds) { ... }
}
```

### 数据权限使用

```java
// Controller 上加注解，指定 SQL 中的别名
@DataScope(deptAlias = "d", userAlias = "u")
@GetMapping("/list")
public TableDataInfo list(SysUser user) { ... }

// Mapper XML 中: SELECT ... FROM xxx x LEFT JOIN sys_dept d ON x.dept_id = d.dept_id
//                 WHERE ... ${params.dataScope}
// DataScopeAspect 自动将 ${params.dataScope} 替换为:
//   " AND (d.dept_id = ? OR d.company_id = ? OR ...) "
```

### 文件上传

- `CommonController.upload()` 统一接收 → 保存到 `{profile}/upload/` → 返回访问路径
- Web 端: `FormData` + axios
- 小程序: `uni.uploadFile` + `name: 'file'`
- 配置路径: `application.yml` → `xyjiance.profile`

---

## 前端开发模式（Web — xyjiance-ui）

### 页面开发

- 页面组件放在 `src/views/{module}/` 下
- 路由通过 `sys_menu` 表配置（`component` 字段指向 `@/views/` 下的 `.vue` 文件）
- `src/api/` 下按模块建 API 文件，使用 `@/utils/request.js` 封装的 axios 实例

### 权限控制

```vue
<!-- 按钮权限 -->
<el-button v-hasPermi="['system:user:add']">新增</el-button>
<!-- 角色权限 -->
<el-button v-hasRole="['admin']">管理员功能</el-button>
```

### 工具函数（`@/utils/xyjiance.js`）

- `parseTime(date, pattern)` — 日期格式化
- `selectDictLabel(dictList, value)` — 字典值 → 标签
- `handleTree(data, 'id', 'parentId')` — 构造树形数据
- `tansParams(params)` — GET 请求参数序列化
- `addDateRange(params, dateRange)` — 添加日期范围查询

### 环境变量

前缀 `VITE_`，定义在 `.env.development` / `.env.production` / `.env.staging`:
- `VITE_APP_TITLE` — 网页标题
- `VITE_APP_BASE_API` — API 前缀（开发环境 `/dev-api`）
- `VITE_APP_ENV` — 环境标识

---

## 小程序开发模式（xyjiance-app）

### 与 Web 端的核心差异

| | Web (xyjiance-ui) | 小程序 (xyjiance-app) |
|---|---|---|
| 路由 | Vue Router，由 `sys_menu` 动态生成 | `pages.json` 静态配置 + TabBar |
| HTTP | axios + `@/utils/request.js` | `uni.request` + 拦截器 |
| Token 存储 | js-cookie | `uni.setStorageSync('Admin-Token')` |
| UI 组件 | Element Plus | uView Plus（`u-button`, `u-input`, `u-cell` 等） |
| 页面跳转 | `router.push()` | `uni.navigateTo()` / `uni.switchTab()` / `uni.reLaunch()` |
| 生命周期 | `mounted`, `created` | `onLoad`, `onShow`, `onReady`, `onHide` |
| 下拉刷新 | — | `pages.json` 中 `enablePullDownRefresh: true` |

### ⚠️ Element Plus 不可用于小程序端

小程序只能使用 uView Plus 或 uni-ui 组件。如果用条件编译可以写混用代码。

### 微信登录流程

```
1. uni.login({ provider: 'weixin' }) → code
2. POST /wx/login { code } → token（已绑定）或 601（未绑定）
3. 601 → 引导用户输入系统账号密码 → 调绑定接口 → 再调 /wx/login
```

### 条件编译

```vue
<!-- 仅微信小程序 -->
<!-- #ifdef MP-WEIXIN -->
<button open-type="getPhoneNumber">微信手机号</button>
<!-- #endif -->

<!-- 仅 H5 -->
<!-- #ifdef H5 -->
<u-input v-model="phone" placeholder="请输入手机号" />
<!-- #endif -->
```

### 页面配置（`pages.json`）

- 新页面加到 `pages` 数组
- TabBar 页面加到 `tabBar.list`（最少 2 个，最多 5 个）
- easycom 自动注册 `u-*` 组件，无需手动 import

---

## 常用注解速查

| 注解 | 位置 | 作用 |
|------|------|------|
| `@Anonymous` | xyjiance-common | 标记接口可匿名访问 |
| `@Log(title, businessType)` | xyjiance-common | 操作日志记录 |
| `@DataScope(deptAlias, userAlias)` | xyjiance-common | 数据权限 SQL 注入 |
| `@DataSource(DataSourceType.SLAVE)` | xyjiance-common | 切换数据源(主/从) |
| `@RateLimiter(key, time, count)` | xyjiance-common | 接口限流 |
| `@RepeatSubmit(interval)` | xyjiance-common | 防重复提交 |
| `@Sensitive(type)` | xyjiance-common | 数据脱敏(字段级) |
| `@Excel(name)` / `@Excels` | xyjiance-common | Excel 导入导出列映射 |
| `@PreAuthorize("@ss.hasPermi('xxx')")` | Spring | 按钮/接口权限 |

---

## 配置文件索引

| 文件 | 内容 |
|------|------|
| `xyjiance-admin/.../application.yml` | 端口、Redis、Token、MyBatis、SpringDoc、XSS、防盗链 |
| `xyjiance-admin/.../application-druid.yml` | 主从数据源、Druid 连接池、监控登录 |
| `xyjiance-admin/.../logback.xml` | 日志路径 `/home/xyjiance/logs`、级别 `com.xyjiance` |
| `xyjiance-generator/.../generator.yml` | 代码生成包路径 `com.xyjiance.system`、作者 |
| `xyjiance-ui/vite.config.js` | Vite + API 代理 + 插件 |
| `xyjiance-app/vite.config.js` | uni-app Vite + API 代理 |
| `xyjiance-app/src/manifest.json` | 微信小程序 appid 配置 |
| `xyjiance-app/src/pages.json` | 小程序路由 + TabBar |

### 重要配置默认值

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| 密码错误锁定 | 5次/10分钟 | `user.password.maxRetryCount` / `lockTime` |
| 文件上传限制 | 单文件 10MB，总请求 20MB | `spring.servlet.multipart` |
| Token 密钥 | `abcdefghijklmnopqrstuvwxyz` | **生产环境必须更换** |
| Token 有效期 | 30分钟（最后20分钟自动续期） | `token.expireTime` |
| 验证码类型 | `math`（数字计算） | 可选 `char`（字符验证码） |
| Druid 监控 | `/druid/`，账号 `xyjiance`/`123456` | `application-druid.yml` |
| 文件上传路径 | Windows: `D:/xyjiance/uploadPath`，Linux: `/home/xyjiance/uploadPath` | `xyjiance.profile` |
| XSS 过滤 | 默认开启，排除 `/system/notice` | 匹配模式 `/system/*,/monitor/*,/tool/*` |
| API 文档 | `/swagger-ui.html` | SpringDoc OpenAPI 3.0 |

---

## 开发注意事项速查

### 后端

1. 新增业务表必须加 `company_id` 字段，否则公司级数据隔离失效
2. Mapper XML 查询必须引用 `${params.dataScope}`，否则 `@DataScope` 不生效
3. Controller 查询方法入参必须是 `BaseEntity` 子类
4. 超级管理员 (`userId=1`) 自动跳过所有数据权限过滤
5. 微信登录的 `code2session` 需在小程序后台配置真实 AppID/Secret，当前代码为占位（`WxLoginService.java:47`）
6. 项目当前无自动化测试（无 JUnit/Mockito 依赖），所有验证需手动进行

### Web 前端

1. 新页面需要在 `sys_menu` 表插入记录才能动态加载路由
2. 按钮权限用 `v-hasPermi`，角色权限用 `v-hasRole`
3. Token 过期（401）会自动弹窗引导重新登录
4. API 请求通过 `/dev-api` 代理到后端，生产环境需 Nginx 反向代理

### 小程序

1. 不用 Router，页面跳转用 uni API（`navigateTo`/`switchTab`/`reLaunch`）
2. 不能用 Element Plus，必须用 uView Plus（`u-button`, `u-input` 等）
3. Token 用 `uni.setStorageSync`，生命周期用 `onLoad`/`onShow`
4. 新页面需在 `pages.json` 中注册
5. 微信登录初次需要绑定已有系统账号（处理 601 状态码）
6. `vite.config.js` 中的 API 代理在小程序端不生效，生产环境需配置真实 API 域名

### 三端通用

1. API 响应格式统一: `{ code: 200, msg: "...", data: ... }` / 分页: `{ rows: [...], total: N }`
2. 所有认证请求 Header: `Authorization: Bearer <token>`
3. 文件上传统一走 `/common/upload`
4. 字典数据前端缓存，减少重复请求
