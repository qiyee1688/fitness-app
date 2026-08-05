# Fitness App 开发进度

**最后更新**：2026-08-05

---

## 📊 总体进度

- **领域建模**：✅ 完成（40 条决策已落 CONTEXT.md）
- **项目初始化**：✅ 完成
- **切片 1**：✅ 完成（Exercise 数据导入 + API + 前端视觉 QA）
- **切片 2**：✅ 完成（UserProfile API + 前端表单 + 中英文展示）
- **切片 3**：✅ 完成（Plan Generator + Plan View）
- **切片 4**：✅ 完成（Today Workout 查询 + 幂等打卡 + 前端视觉 QA）
- **切片 5**：✅ 完成（ExerciseFeedback + HURT 即时替换 + 4 周安全过滤）
- **切片 6**：✅ 完成（Plan 7 态生命周期 + 排队激活 + 自动续期）
- **PRD / Issues**：✅ PRD 已发布，implementation issues 已拆分

---

## 🎯 当前冲刺：MVP 主路径

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
- [x] 完成 UserProfile 端到端：后端持久化、统一响应 API、前端表单、语言切换

### 待办 ⏳
- [x] 前端详情页做视觉走查与交互微调
- [x] 安装并验证 `gh` CLI
- [x] 发布 MVP PRD 到 GitHub Issues：[#1 PRD: Fitness Coaching App MVP](https://github.com/qiyee1688/fitness-app/issues/1)
- [x] 运行 `/to-issues` 拆分 PRD
- [x] 整理 App 开发计划与简单功能说明：[`docs/app-development-plan.md`](docs/app-development-plan.md)
- [x] 完成 [#3 UserProfile End-to-End](https://github.com/qiyee1688/fitness-app/issues/3)
- [x] 完成 [#4 Plan Generator MVP](https://github.com/qiyee1688/fitness-app/issues/4)
- [x] 实现纯 Plan Generator、持久化 Service 与 `POST /api/plans/generate`
- [x] 应用 `20260805_add_plan_optimistic_lock.sql` 并完成真实 API 持久化验证
- [x] 完成 [#5 Plan View](https://github.com/qiyee1688/fitness-app/issues/5)
- [x] 实现 ACTIVE Plan 查询、8 周查看页、处方展示和 Exercise 详情跳转
- [x] 完成 [#6 Today Workout Check-in](https://github.com/qiyee1688/fitness-app/issues/6)
- [x] 实现当天 Workout 查询、原子幂等打卡、Today 页面和中英文状态展示
- [x] 完成 [#7 ExerciseFeedback and HURT Substitution](https://github.com/qiyee1688/fitness-app/issues/7)
- [x] 实现四类 Exercise 反馈、HURT 身体部位记录、即时安全替换/移除和未来 4 周过滤状态
- [x] 完成 [#8 Plan Lifecycle Automation](https://github.com/qiyee1688/fitness-app/issues/8)
- [x] 实现未来 SCHEDULED、ACTIVE → PAUSED、PAUSED → CANCELLED、到期 COMPLETED + child Plan 自动续期

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
  - 关闭 GitHub Issue [#2](https://github.com/qiyee1688/fitness-app/issues/2)
- **验证**：
  - `mvn test` 通过（8 tests）
  - `npm run build` 通过
  - API 验证：`bodyPart=upper legs,lower legs` 返回 286 条
- **下一步**：进入 [#3 UserProfile End-to-End](https://github.com/qiyee1688/fitness-app/issues/3)

### 会话 9：2026-08-03
- **任务**：Exercise 页面中英文展示与语言切换
- **完成**：
  - 增加全局语言状态，支持中文 / English 切换，并持久化到 `localStorage`
  - 支持 `?lang=zh` / `?lang=en` 初始化语言，方便回归测试
  - 顶栏、导航、搜索、空状态、按钮、详情字段名支持中英文
  - Exercise 列表和详情页的 BodyPart、Target、Equipment、Muscle Group、Secondary 标签支持中文展示
  - Exercise 详情步骤按语言切换：中文优先 `instructionSteps.zh`，英文优先 `instructionSteps.en`
  - 常见 Exercise 名称增加中文展示规则，原始数据保持不变
  - 移动端顶栏调整为多行布局，避免语言切换控件被裁切
- **验证**：
  - `mvn test` 通过（8 tests）
  - `npm run build` 通过
  - Chrome headless 截图检查：中文桌面列表、中文移动列表、英文桌面列表、英文详情页
- **下一步**：继续进入 [#3 UserProfile End-to-End](https://github.com/qiyee1688/fitness-app/issues/3)

### 会话 10：2026-08-03
- **任务**：实现 [#3 UserProfile End-to-End](https://github.com/qiyee1688/fitness-app/issues/3)
- **完成**：
  - 新增 User / UserProfile 领域对象、FitnessLevel / Goal 枚举、UserProfile DTO
  - 新增 `GET /api/users/profile` 与 `POST /api/users/profile`，返回统一 `{code, message, data}`
  - 业务逻辑下沉到 `UserService`，Controller 只做参数校验与响应包装
  - MyBatis XML 使用参数化 SQL，并对 PostgreSQL UUID / enum / JSONB 做显式 cast
  - 前端新增用户档案页，支持训练水平、训练目标、每周训练天数、可用器械、加载/错误/保存状态
  - 用户档案页面接入中文 / English 语言切换，中文展示已去除混用英文标签
- **验证**：
  - `mvn test` 通过（15 tests）
  - `npm run build` 通过
  - 真实 API 验证通过：读取 demo 档案、更新为 `INTERMEDIATE / MUSCLE_GAIN / 4 days`、再次读取确认已持久化
- **下一步**：进入 [#4 Plan Generator MVP](https://github.com/qiyee1688/fitness-app/issues/4)

### 会话 11：2026-08-03
- **任务**：补充后端和前端服务启动、重启等开发脚本
- **完成**：
  - 新增 `scripts/backend.sh`，支持 `start|stop|restart|status|logs|test|seed`
  - 新增 `scripts/frontend.sh`，支持 `start|stop|restart|status|logs|build`
  - 新增 `scripts/middleware.sh`，统一管理 PostgreSQL / Redis Docker Compose
  - 新增 `scripts/dev.sh`，提供全栈 `start|stop|restart|status|logs|test|build`
  - 根 README 增加脚本使用说明，修正中间件 README 的宿主机端口
- **验证**：
  - Shell 脚本语法检查通过
  - `scripts/backend.sh test` 通过（15 tests）
  - `scripts/frontend.sh build` 通过
- **下一步**：继续进入 [#4 Plan Generator MVP](https://github.com/qiyee1688/fitness-app/issues/4)

### 会话 12：2026-08-03
- **任务**：修复后端启动脚本失败
- **完成**：
  - 后端脚本不再直接信任开发者本机旧 `JAVA_HOME`，优先使用项目固定 Java 21.0.7
  - 自动把项目 Maven 3.9.9 加入 `PATH`，降低 shell 环境差异影响
  - 后端 `start` 会等待 Spring Boot 启动成功；若进程提前退出，直接打印日志尾部并返回非 0
  - 前后端后台启动改为 `nohup`，日志中记录实际 Java / Maven / Node / npm 版本
- **验证**：
  - Shell 脚本语法检查通过
  - 模拟坏环境 `JAVA_HOME=/usr PATH=/usr/bin:/bin:/usr/sbin:/sbin scripts/backend.sh test` 通过（15 tests）
- **下一步**：继续进入 [#4 Plan Generator MVP](https://github.com/qiyee1688/fitness-app/issues/4)

### 会话 13：2026-08-05
- **任务**：实现 [#4 Plan Generator MVP](https://github.com/qiyee1688/fitness-app/issues/4)
- **完成**：
  - 新增独立 `PlanGenerator`，按 `daysPerWeek` 降级生成默认 8 周 Plan
  - 生成 Push / Pull / Legs / FullBody Workout，并按 Focus 附加 Core Exercise
  - 生成含 sets / reps / loadType / rpe 的 Prescription
  - Plan 保存 UserProfile JSONB 快照，后续档案修改不影响已生成 Plan
  - 新增 Plan / Workout / Prescription 参数化 MyBatis 持久化
  - 新增 `POST /api/plans/generate`，保持统一 API 响应和 Service 业务边界
  - ACTIVE Plan 切换使用 `version` 乐观锁，并增加单用户 ACTIVE Plan 部分唯一索引
- **验证**：
  - `mvn test` 通过（21 tests）
  - Spring Boot 在 8081 端口启动成功并连接本地 PostgreSQL
  - 已应用 `20260805_add_plan_optimistic_lock.sql`，确认 `version` 列与单 ACTIVE 部分唯一索引生效
  - 真实生成与重生成 API 均返回成功，每条 Plan 含 32 个 Workout、144 个 Prescription
  - 重生成后旧 Plan 为 `SUPERSEDED(version=1)`，新 Plan 为 `ACTIVE(version=0)`，用户 ACTIVE Plan 数量为 1
  - 新 Plan 的 UserProfile JSONB 快照与 demo 档案一致
- **下一步**：进入 [#5 Plan View](https://github.com/qiyee1688/fitness-app/issues/5)


### 会话 14：2026-08-05
- **任务**：完成 #5 Plan View
- **完成**：
  - 新增 `GET /api/plans/current`，Service 层聚合 ACTIVE Plan、Workout、Prescription 和 Exercise 摘要
  - 使用 3 次批量查询避免 Workout 级 N+1
  - 新增中英文 8 周 Plan 页面、周切换、加载/空数据/无 ACTIVE Plan/API 错误状态
  - Prescription 可跳转至现有 Exercise 详情页
  - 真实数据验证：8 周、32 个 Workout、144 个 Prescription
  - `mvn test` 24/24 通过，`npm run build` 通过
- **下一步**：进入 #6 Today Workout Check-in

### 会话 15：2026-08-05
- **任务**：完成 [#6 Today Workout Check-in](https://github.com/qiyee1688/fitness-app/issues/6)
- **完成**：
  - 新增 `GET /api/plans/today`，按 ACTIVE Plan 的 `startDate` 和请求日期确定当天 Workout
  - 新增 `POST /api/plans/workouts/{workoutId}/complete`，使用条件更新原子写入 `completed_at`
  - 重复打卡保持幂等，返回原完成时间并标记 `alreadyCompleted=true`
  - 新增中英文 Today 页面，覆盖加载、错误、无 ACTIVE Plan、休息日和已完成状态
  - 展示顺序 Prescription、sets、reps、RPE、可读 LoadType，并支持跳转 Exercise 详情
- **验证**：
  - `mvn test` 30/30 通过
  - `npm run build` 通过
  - 真实 API 首次打卡写入 `2026-08-05T13:55:36.454657`，重复调用返回相同时间
  - 浏览器验证中文“自重 / 按 RPE”、英文“Body weight / RPE only”，并实际跳转 `/exercises/3293`
- **下一步**：进入 [#7 ExerciseFeedback and HURT Substitution](https://github.com/qiyee1688/fitness-app/issues/7)

### 会话 16：2026-08-05
- **任务**：完成 [#7 ExerciseFeedback and HURT Substitution](https://github.com/qiyee1688/fitness-app/issues/7)
- **完成**：
  - 新增 ExerciseFeedback 领域对象、请求/响应 DTO 和统一反馈 API
  - 普通反馈只持久化；HURT 保存身体部位和 4 周过滤截止日期
  - HURT 在 Service 层优先安全替换当前 Prescription，无候选时标记安全移除
  - 替换/移除使用带原 Exercise 和有效状态条件的参数化更新，冲突返回统一 409 错误码
  - Today 页面支持 TOO_EASY、JUST_RIGHT、TOO_HARD、HURT 与身体部位输入，并使用返回的 Workout 原位刷新
- **验证**：
  - `mvn test` 33/33 通过，`npm run build` 通过
  - 真实普通反馈落库且 Workout 不变
  - 真实 HURT 反馈保存为 `HURT_WAIST`，`filter_until=2026-09-02`，并将 Exercise `3293` 替换为 `0690`
  - 浏览器验证中英文反馈选项、HURT 身体部位输入和页面提交成功提示
- **下一步**：进入 [#8 Plan Lifecycle Automation](https://github.com/qiyee1688/fitness-app/issues/8)

### 会话 17：2026-08-05
- **任务**：完成 [#8 Plan Lifecycle Automation](https://github.com/qiyee1688/fitness-app/issues/8)
- **完成**：
  - 未来开始日期生成 SCHEDULED Plan，不替换当前 ACTIVE Plan
  - 无 ACTIVE 时按 startDate 激活最早到期的 SCHEDULED Plan
  - ACTIVE 连续 2 周无打卡转 PAUSED，PAUSED 再 2 周未恢复转 CANCELLED
  - 8 周到期将父 Plan 标 COMPLETED，并自动创建带 parentPlanId 的 ACTIVE child Plan
  - 新增 status_changed_at 审计字段，所有状态迁移使用 expectedStatus + version 乐观锁
  - 新增统一生命周期处理 API：`POST /api/plans/lifecycle/process`
- **验证**：
  - Service 测试覆盖 SCHEDULED、PAUSED、CANCELLED、COMPLETED、child renewal 和乐观锁冲突
  - 本地 PostgreSQL 迁移成功，真实 API 幂等调用保持现有 ACTIVE Plan 不变
- **下一步**：PR #9、#10、#11 已依次合并；完成 main 主路径回归并收口兼容性告警


### 会话 18：2026-08-05
- **任务**：合并 MVP 堆叠 PR 并执行 main 集成验收
- **完成**：
  - PR #9、#10、#11 按依赖顺序合入 main
  - PostgreSQL/Redis 健康，三个迁移的关键字段和索引核对通过
  - 修复 Element Plus radio/checkbox button 旧 label value API 警告
- **验证**：
  - `mvn test` 40/40 通过
  - `npm run build` 通过
  - 浏览器验证动作库、8 周 Plan、Today 休息日和 UserProfile 页面
- **下一步**：合并 MVP 集成验收 PR，关闭 MVP PRD issue
