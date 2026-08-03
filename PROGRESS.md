# Fitness App 开发进度

**最后更新**：2026-08-03

---

## 📊 总体进度

- **领域建模**：✅ 完成（40 条决策已落 CONTEXT.md）
- **项目初始化**：✅ 完成
- **切片 1**：🚧 收尾中（主干完成，测试与视觉 QA 收口）
- **切片 2**：⏳ 待开始
- **切片 3**：⏳ 待开始
- **切片 4**：⏳ 待开始

---

## 🎯 当前冲刺：项目初始化

### 已完成 ✅
- [x] 领域建模（CONTEXT.md）— 40 条决策
- [x] 技术栈选型（Spring Boot 3.5.16 + Java 21 + MyBatis + PostgreSQL + Vue 3.5.40）
- [x] 项目框架文档（PROJECT.md）
- [x] 进度追踪文档（PROGRESS.md）
- [x] 创建修改记录文档（CHANGELOG.md）
- [x] 初始化 Spring Boot 项目（pom.xml + 目录结构）
- [x] 配置 MyBatis（application.yml + XML Mapper）
- [x] 编写 docker-compose.yml（PostgreSQL + Redis 容器，位于 local-middleware）
- [x] 设计 exercises 表结构（schema.sql）
- [x] 初始化 Vue 3 前端项目（Vite + Element Plus）
- [x] 实现 Exercise 列表页与详情页骨架

### 切片 1 主干 ✅
- [x] 下载 exercises-dataset 数据
- [x] 写 Exercise 数据导入器
- [x] 导入 1,324 条 Exercise 数据到本地 PostgreSQL
- [x] 联调 `GET /api/exercises/{id}` 与前端 Vite 代理
- [x] 为 Exercise API 补 Controller 测试

### 待办 ⏳
- [ ] 前端详情页做视觉走查与交互微调

---

## 📅 里程碑计划

### 里程碑 1：切片 1 完成（预计 1-2 天）
- **目标**：Exercise 数据导入 + 详情页可用
- **交付物**：
  - PostgreSQL 中有 1,324 条 Exercise 数据
  - `GET /api/exercises/{id}` API 可用
  - 前端 Exercise 详情页展示 GIF + 步骤说明

### 里程碑 2：切片 2 完成（预计 +1 天）
- **目标**：用户可创建/更新档案
- **交付物**：
  - 用户档案表结构
  - `POST /api/users/profile` API 可用
  - 前端档案表单（目标/水平/天数/器械）

### 里程碑 3：切片 3 完成（预计 +2-3 天）
- **目标**：用户可生成 8 周 Plan
- **交付物**：
  - Plan 生成器（简化版，1 个硬编码模板）
  - `POST /api/plans/generate` API 可用
  - 前端查看 Plan 页面（8 周日历）

### 里程碑 4：切片 4 完成（预计 +1-2 天）
- **目标**：用户可打卡今日 Workout
- **交付物**：
  - Workout 打卡逻辑
  - ExerciseFeedback 表结构
  - 前端今日 Workout 页 + 反馈弹窗
  - **一期 MVP 完成** 🎉

---

## 🐛 已知问题

*暂无*

---

## 💡 待决策事项

*暂无（所有开放分叉已关闭）*

---

## 📝 会话记录

### 会话 1：2026-08-03
- **任务**：领域建模 + 项目初始化
- **完成**：
  - CONTEXT.md 完成（40 条决策）
  - 技术栈确定（Spring Boot 3.5.16 + Java 21 + MyBatis + Vue 3.5.40）
  - PROJECT.md 创建
  - PROGRESS.md 创建
- **下一步**：创建 CHANGELOG.md，然后开始切片 1

### 会话 2：2026-08-03
- **任务**：指定 Java/Vue 版本，并按 CONTEXT.md 继续项目初始化
- **完成**：
  - Java 锁定为 21 LTS（目标 21.0.7）
  - Spring Boot 升级到 3.5.16，MyBatis Starter 升级到 3.0.5
  - Vue 锁定为 3.5.40，Vite 锁定为 8.2.0
  - 后端开启 Virtual Threads
  - 后端 API 改为统一 `{code, message, data}` 响应，并加入全局异常处理器
  - `schema.sql` 对齐 CONTEXT.md 的 Goal / PlanStatus / LoadType / ExerciseFeedback 决策
  - 初始化 frontend，完成 Exercise 列表页与详情页骨架
  - 下载 exercises-dataset 主 JSON，导入本地 PostgreSQL 共 1,324 条
  - 验证后端详情 API、分页 API、Vite `/api` 代理
- **下一步**：为 Exercise API 补测试（已在会话 3 完成），并做前端页面视觉走查

### 会话 3：2026-08-03
- **任务**：收口切片 1 测试与状态
- **完成**：
  - 新增 Exercise Controller MVC 测试，覆盖详情成功、详情 404、分页、筛选、搜索、分页参数校验
  - 移除启动类上的 `@MapperScan`，保留 Mapper 接口 `@Mapper` 注解，避免 Web slice 测试误加载 MyBatis FactoryBean
  - 重启后端并验证 `GET /api/exercises/0001` 仍返回 200
  - 修正总体进度状态：项目初始化完成，切片 1 进入收尾
- **下一步**：前端详情页视觉走查与交互微调，然后切片 1 关账
