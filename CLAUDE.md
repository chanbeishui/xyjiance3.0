# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run Commands

**后端 (Spring Boot 4.x, JDK 17+, Maven):**
- 启动后端: 运行 `ruoyi-admin/src/main/java/com/ruoyi/RuoYiApplication.java` 的 main 方法
- 打包: `mvn clean package -DskipTests`
- 后端默认端口: `8080`

**前端 (Vue 2 + Vue CLI):**
- 前端目录: `ruoyi-ui/`
- 安装依赖: `npm install`
- 启动开发服务器: `npm run dev` (默认端口 80，API 代理到 `localhost:8080`)
- 生产构建: `npm run build:prod`
- 预发布构建: `npm run build:stage`
- 预览构建产物: `npm run preview`

**数据库:**
- 数据库脚本: `sql/ry_20260417.sql` (主库) 和 `sql/quartz.sql` (定时任务)
- 数据库类型: MySQL，连接信息在 `application-druid.yml`
- 默认数据库名: `ry-vue`

## 项目架构

本项目是若依 (RuoYi) Spring Boot + Vue 前后端分离版本，master 分支基于 **Spring Boot 4.x + JDK 17**。

### 后端模块结构 (Maven 多模块)

| 模块 | 说明 |
|------|------|
| `ruoyi-admin` | 入口模块，含启动类 `RuoYiApplication` 和所有 Controller |
| `ruoyi-common` | 通用模块：实体类、工具类、注解（@Log, @DataScope, @Anonymous, @RateLimiter 等）、异常定义、常量和枚举 |
| `ruoyi-framework` | 框架层：Spring Security 配置、JWT 认证、AOP 切面（日志、数据权限、数据源切换、限流）、全局异常处理、Token 服务 |
| `ruoyi-system` | 系统业务模块：用户、角色、菜单、部门、岗位、字典、通知、日志等，含 Service 接口/实现和 Mapper |
| `ruoyi-generator` | 代码生成器模块，基于 Velocity 模板生成前后端 CRUD 代码 |
| `ruoyi-quartz` | 定时任务模块，基于 Quartz 的任务调度 |

### 后端分层架构

每个业务模块遵循 Controller → Service(IService/ServiceImpl) → Mapper 三层结构。
- Controller 层在 `ruoyi-admin/web/controller/` 下按功能分包 (`system/`, `monitor/`, `common/`, `tool/`)
- Service 和 Mapper 在 `ruoyi-system` 中定义
- 实体类分为：`ruoyi-common/core/domain/entity/` (核心实体，如 SysUser, SysRole) 和 `ruoyi-system/domain/` (模块特定实体)
- 请求/响应模型在 `ruoyi-common/core/domain/model/` (如 LoginUser, LoginBody)
- 通用响应类: `AjaxResult` (单条/操作响应), `TableDataInfo` (分页列表响应), `R` (返回值)

### 关键架构设计

**认证与权限:**
- Spring Security + JWT 无状态认证，不使用 Session
- `JwtAuthenticationTokenFilter` 拦截请求，从 Header 中解析 token
- `@Anonymous` 注解标记可匿名访问的接口
- 菜单/权限从后端 `SysMenu` 表动态获取，登录后返回角色和权限标识，前端根据权限动态生成路由
- 按钮级权限通过 `@PreAuthorize` 或 `@ss` 注解配合权限字符控制
- `@DataScope` 注解实现数据权限（按部门过滤 SQL），由 `DataScopeAspect` 切面实现

**数据架构:**
- 主从数据库切换: `@DataSource` 注解 + `DynamicDataSourceContextHolder` + AOP
- 分页: PageHelper 插件，`TableSupport.buildPageRequest()` 构建分页
- 数据库连接池: Druid，监控页面 `/druid/`

**缓存:**
- Redis 用作 token 存储和系统参数/字典缓存
- `RedisCache` 是 Redis 操作的工具封装

**前端路由机制:**
- 路由分两类：`constantRoutes` (无需权限，如登录页/404) 和 `dynamicRoutes` (需要权限，由后端 `getRouters` 接口返回)
- 前端 `permission.js` store 调用 `/getRouters` 接口，后端返回当前用户可见的路由树
- 前端将后端返回的路由转换为 Vue Router 动态路由，添加到 `Layout` 组件的 `children` 中
- 侧边栏和顶栏菜单根据 `sidebarRouters` / `topbarRouters` 渲染
- `@/layout` 是主布局组件，`@/views` 是各页面视图

**代码生成器 (`ruoyi-generator`):**
- 读取数据库表结构，使用 Velocity 模板生成 Controller/Service/Mapper/Entity/Vue 页面/SQL
- 生成的文件自动带 CRUD 功能和 Excel 导入导出

### 配置文件说明

- `ruoyi-admin/src/main/resources/application.yml` — 主配置：服务端口、Redis、token、MyBatis、Springdoc、XSS 过滤等
- `ruoyi-admin/src/main/resources/application-druid.yml` — 数据源配置：主从库、Druid 连接池
- `ruoyi-generator/src/main/resources/generator.yml` — 代码生成器配置
- `ruoyi-ui/vue.config.js` — 前端开发服务器代理配置，`VUE_APP_BASE_API` 环境变量控制 API 前缀

### 常用注解一览

| 注解 | 位置 | 作用 |
|------|------|------|
| `@Anonymous` | `ruoyi-common` | 标记接口可匿名访问 |
| `@Log` | `ruoyi-common` | 操作日志记录 |
| `@DataScope` | `ruoyi-common` | 数据权限过滤，配合用户/部门别名 |
| `@DataSource` | `ruoyi-common` | 切换数据源(主/从) |
| `@RateLimiter` | `ruoyi-common` | 接口限流 |
| `@RepeatSubmit` | `ruoyi-common` | 防止重复提交 |
| `@Sensitive` | `ruoyi-common` | 数据脱敏(字段级) |
| `@Excel` / `@Excels` | `ruoyi-common` | Excel 导入导出列映射 |
