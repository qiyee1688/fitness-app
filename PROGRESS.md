# Fitness App 开发进度

**最后更新**：2026-08-03

---

## 📊 总体进度

- **领域建模**：✅ 完成（40 条决策已落 CONTEXT.md）
- **项目初始化**：✅ 完成
- **切片 1**：✅ 完成（Exercise 数据导入 + API + 前端视觉 QA）
- **切片 2**：⏳ 待开始
- **切片 3**：⏳ 待开始
- **切片 4**：⏳ 待开始
- **PRD / Issues**：✅ PRD 已发布，implementation issues 已拆分

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
- [x] 前端详情页做视觉走查与交互微调
- [x] 安装并验证 `gh` CLI
- [x] 发布 MVP PRD 到 GitHub Issues：[#1 PRD: Fitness Coaching App MVP](https://github.com/qiyee1688/fitness-app/issues/1)
- [x] 运行 `/to-issues` 拆分 PRD
- [x] 整理 App 开发计划与简单功能说明：[`docs/app-development-plan.md`](docs/app-development-plan.md)

### GitHub Issues ✅
- [x] [#1 PRD: Fitness Coaching App MVP](https://github.com/qiyee1688/fitness-app/issues/1)
- [x] [#2 Finish Exercise Detail Visual QA / 收口 Exercise 详情页视觉 QA](https://github.com/qiyee1688/fitness-app/issues/2)
- [x] [#3 UserProfile End-to-End / 用户档案端到端](https://github.com/qiyee1688/fitness-app/issues/3)
- [x] [#4 Plan Generator MVP / 8 周 Plan 生成器 MVP](https://github.com/qiyee1688/fitness-app/issues/4)
- [x] [#5 Plan View / Plan 查看页](https://github.com/qiyee1688/fitness-app/issues/5)
- [x] [#6 Today Workout Check-in / 今日 Workout 打卡](https://github.com/qiyee1688/fitness-app/issues/6)
- [x] [#7 ExerciseFeedback and HURT Substitution / ExerciseFeedback 与 HURT 替换](https://github.com/qiyee1688/fitness-app/issues/7)
- [x] [#8 Plan Lifecycle Automation / Plan 生命周期自动化](https://github.com/qiyee1688/fitness-app/issues/8)

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

### 会话 4：2026-08-03
- **任务**：配置 Matt skills 并进入 `/to-prd`
- **完成**：
  - 配置 GitHub Issues 作为 issue tracker
  - 配置默认 triage labels
  - 配置 single-context domain docs
  - 创建中英双语 `AGENTS.md` 与 `docs/agents/*.md`
  - 创建 MVP PRD 本地草稿 `docs/prd/fitness-app-mvp-prd.md`
- **阻塞**：
  - 当前环境没有 `gh` CLI，暂未能发布 PRD 到 GitHub Issues
- **下一步**：安装或配置 `gh` CLI，发布 PRD issue 并运行 `/to-issues`

### 会话 5：2026-08-03
- **任务**：安装 `gh` 并发布 MVP PRD
- **完成**：
  - 通过 Homebrew 安装 GitHub CLI：`gh 2.86.0`
  - 验证 GitHub 登录账号：`qiyee1688`
  - 补齐 GitHub triage labels：`needs-triage`、`needs-info`、`ready-for-agent`、`ready-for-human`
  - 发布 MVP PRD 到 GitHub Issues：[#1 PRD: Fitness Coaching App MVP](https://github.com/qiyee1688/fitness-app/issues/1)
- **下一步**：运行 `/to-issues`，把 PRD 拆成可独立领取的 implementation issues

### 会话 6：2026-08-03
- **任务**：运行 `/to-issues` 拆分 MVP PRD
- **完成**：
  - 按 tracer-bullet vertical slice 方式拆分 PRD
  - 创建 7 个 implementation issues，并全部打 `ready-for-agent`
  - 核对 GitHub Issues 列表，确认 #1-#8 均为 open
- **下一步**：优先领取 [#2](https://github.com/qiyee1688/fitness-app/issues/2) 收口切片 1，或进入 [#3](https://github.com/qiyee1688/fitness-app/issues/3) 开始 UserProfile 端到端

### 会话 7：2026-08-03
- **任务**：整理 App 开发计划及简单功能说明
- **完成**：
  - 新增 [`docs/app-development-plan.md`](docs/app-development-plan.md)
  - 汇总产品定位、一期 MVP 功能范围、开发阶段、GitHub Issues 对应关系、暂不做范围和验证标准
- **下一步**：按计划优先处理 [#2](https://github.com/qiyee1688/fitness-app/issues/2)，完成 Exercise 详情页视觉 QA

### 会话 8：2026-08-03
- **任务**：实现 [#2 Exercise 详情页视觉 QA](https://github.com/qiyee1688/fitness-app/issues/2)
- **完成**：
  - 前端 Exercise 列表增加 BodyPart tabs：全部、练胸、练背、练肩、练腿、练核心
  - 练腿筛选支持 `upper legs + lower legs` 组合查询
  - Exercise 列表增加空状态和远程媒体加载失败 fallback
  - Exercise 详情页补全 Category、BodyPart、Target、Equipment、Muscle Group、Secondary 信息
  - 详情页补充无步骤说明状态和媒体 fallback
  - 后端 MyBatis 条件查询支持参数化的多 BodyPart `IN` 查询
  - 增加 ExerciseService 单元测试覆盖组合 BodyPart 筛选解析
  - 修正本地 Mockito 测试配置，避免 JDK self-attach 限制导致测试不稳定
  - 完成桌面/移动端截图视觉 QA
- **验证**：
  - `mvn test` 通过（8 tests）
  - `npm run build` 通过
  - API 验证：`bodyPart=upper legs,lower legs` 返回 286 条
- **下一步**：进入 [#3 UserProfile End-to-End](https://github.com/qiyee1688/fitness-app/issues/3)
