# Fitness App 修改记录

记录每次会话的文件创建/修改，用于快速恢复上下文。

---

## 2026-08-05 会话 17：Plan Lifecycle Automation

### 创建的文件

- **`backend/src/main/java/com/fitness/dto/PlanLifecycleResponse.java`** — 状态迁移统一响应
- **`backend/src/main/resources/migration/20260805_add_plan_lifecycle.sql`** — 生命周期审计时间和队列索引迁移

### 主要修改

- 未来 Plan 生成为 SCHEDULED，不 supersede 当前 ACTIVE Plan。
- 新增生命周期处理 API，支持 SCHEDULED → ACTIVE、ACTIVE → PAUSED、PAUSED → CANCELLED。
- ACTIVE Plan 到期后以乐观锁标记 COMPLETED，并生成 `parentPlanId` 指向父 Plan 的 ACTIVE child Plan。
- 所有状态变更使用旧状态与 version 条件更新，不删除历史 Plan。
- `status_changed_at` 记录每次状态变化时间，供 PAUSED 超时判断和审计使用。

### 验证

- Service 测试覆盖全部自动迁移、child renewal 和并发冲突。
- 本地迁移成功；真实 API 返回 ACTIVE → ACTIVE、`changed=false`，现有 Plan 状态与 version 未变化。

### 下一步行动

- [ ] 按堆叠顺序合并 MVP PR

## 2026-08-05 会话 16：ExerciseFeedback and HURT Substitution

### 创建的文件

- **`backend/src/main/java/com/fitness/domain/{ExerciseFeedback,FeedbackType}.java`** — Exercise 反馈领域模型
- **`backend/src/main/java/com/fitness/dto/{SubmitExerciseFeedbackRequest,ExerciseFeedbackResponse}.java`** — 反馈请求与统一响应 DTO
- **`backend/src/main/resources/migration/20260805_add_feedback_effects.sql`** — HURT 身体部位、4 周过滤截止日期和安全移除字段迁移

### 主要修改

- 新增 `POST /api/plans/workouts/{workoutId}/exercises/{exerciseId}/feedback`。
- Service 层持久化四类反馈；HURT 立即查找安全替代动作，无候选时将当前 Prescription 标记为安全移除。
- HURT 保存标准化身体部位与未来 4 周过滤状态，替换候选排除当前 Workout 重复动作和有效期内受伤 Exercise/肌群。
- Mapper 使用参数化 SQL，并通过 Prescription ID、原 Exercise ID 和 `removed_at IS NULL` 条件保证并发更新安全。
- Today 页面新增中英文反馈控件、HURT 身体部位输入、提交状态和返回 Workout 原位刷新。

### 验证

- `mvn test`：33 tests 全部通过。
- `npm run build`：生产构建成功，仅保留既有的大 chunk 警告。
- 本地 PostgreSQL 迁移成功；真实普通反馈持久化后 Workout 不变。
- 真实 HURT 反馈保存 `HURT_WAIST` 与 `filter_until=2026-09-02`，并将 Exercise `3293` 原子替换为 `0690`。
- 浏览器验证中文/English 四类反馈、HURT 身体部位输入和提交成功提示。

### 下一步行动

- [ ] 进入 #8 Plan Lifecycle Automation

## 2026-08-05 会话 15：Today Workout Check-in

### 创建的文件

- **`backend/src/main/java/com/fitness/dto/TodayWorkoutResponse.java`** — 当天 Workout 与打卡结果 DTO
- **`frontend/src/views/TodayWorkout.vue`** — 中英文今日训练与完成状态页面

### 主要修改

- Plan Mapper 新增当天 Workout、单 Workout、Prescription 查询和 `completed_at IS NULL` 条件更新。
- Plan Service 负责日期到 `dayNumber` 的换算、ACTIVE Plan 归属校验、DTO 塑形和重复打卡幂等语义。
- 新增 `GET /api/plans/today` 与 `POST /api/plans/workouts/{workoutId}/complete`，Controller 保持薄层。
- 前端增加 Today 路由、导航、API 客户端、完成后原位更新，以及完整加载/空态/错误状态。
- LoadType 在中文显示“自重 / 按 RPE”，在英文显示“Body weight / RPE only”。

### 验证

- `mvn test`：30 tests 全部通过。
- `npm run build`：生产构建成功，仅保留既有的大 chunk 警告。
- 真实 API 验证首次完成写入 `2026-08-05T13:55:36.454657`，重复完成返回相同时间且 `alreadyCompleted=true`。
- 浏览器走查中英文 Today 页面，并实际从首条 Prescription 跳转到 `/exercises/3293`。

### 下一步行动

- [ ] 进入 #7 ExerciseFeedback and HURT Substitution

## 2026-08-05 会话 14：Plan View

### 创建的文件

- **`backend/src/main/java/com/fitness/dto/PlanDetailResponse.java`** — ACTIVE Plan 查看 DTO
- **`frontend/src/api/plan.js`** — 当前 Plan API 客户端
- **`frontend/src/views/PlanView.vue`** — 中英文 8 周 Plan 查看页

### 主要修改

- Plan Mapper 批量读取 Workout 与 Prescription/Exercise，避免 N+1 查询。
- Plan Service 负责周次、训练日期和嵌套 DTO 塑形；Controller 保持参数校验与统一响应包装。
- 新增 ACTIVE Plan 不存在错误码，并让前端 HTTP 错误保留业务 code/status。
- 增加 Plan 路由和导航、8 周切换、处方基础信息、Exercise 详情链接，以及加载/空数据/错误状态。
- 增加桌面与移动端响应式样式。

### 验证

- `mvn test`：24 tests 全部通过。
- `npm run build`：生产构建成功。
- 真实 `GET /api/plans/current?username=demo`：ACTIVE Plan 包含 8 周、32 个 Workout、144 个 Prescription。
- 浏览器走查中文、English 页面，并验证首条 Prescription 跳转到 `/exercises/3293`。

### 下一步行动

- [ ] 进入 #6 Today Workout Check-in

## 2026-08-05 会话 13：Plan Generator MVP

### 创建的文件

- **`backend/src/main/java/com/fitness/domain/{Plan,Workout,Prescription}.java`** — Plan 聚合领域对象
- **`backend/src/main/java/com/fitness/domain/{PlanStatus,TrainingDayFocus,LoadType}.java`** — Plan 相关枚举
- **`backend/src/main/java/com/fitness/service/PlanGenerator.java`** — 可脱离 HTTP/数据库测试的 8 周生成器
- **`backend/src/main/java/com/fitness/service/PlanService.java`** — 生成、替换与持久化编排
- **`backend/src/main/java/com/fitness/controller/PlanController.java`** — Plan 生成 API
- **`backend/src/main/java/com/fitness/mapper/PlanMapper.java`** 与 **`backend/src/main/resources/mapper/PlanMapper.xml`** — 参数化 Plan 聚合持久化
- **`backend/src/main/resources/migration/20260805_add_plan_optimistic_lock.sql`** — ACTIVE Plan 乐观锁迁移
- **`backend/src/test/java/com/fitness/{service,controller}/Plan*.java`** — Generator、Service、Controller 测试

### 关键决策

- #4 只实现后端 Plan Generator 垂直切片；Plan 查看 UI 保留给 #5。
- 无 Template 时按 `daysPerWeek` 生成 8 周默认结构，保证 Plan/Workout/Prescription 非空。
- 重生成通过 `plans.version` 乐观锁将旧 ACTIVE Plan 标为 SUPERSEDED，并用部分唯一索引兜底单 ACTIVE 约束。
- 器械候选查询始终允许 `body weight`，并保持 MyBatis 参数化查询。

### 验证

- `mvn test`：21 tests 全部通过。
- Spring Boot 3.5.16：8081 端口启动成功，MyBatis Mapper 加载并连接 PostgreSQL。
- 已应用 `20260805_add_plan_optimistic_lock.sql`，核对 `plans.version` 与 ACTIVE Plan 部分唯一索引。
- 两次真实生成 API 均成功：每条 Plan 32 个 Workout、144 个 Prescription。
- 重生成后旧 Plan 为 `SUPERSEDED(version=1)`，新 Plan 为 `ACTIVE(version=0)`，ACTIVE 数量严格为 1。
- UserProfile 已作为 JSONB 快照写入新 Plan。

### 下一步行动

- [x] 应用 `20260805_add_plan_optimistic_lock.sql`
- [x] 真实调用生成与重生成 API，核对 8 周 Workout/Prescription 数量和旧 Plan SUPERSEDED 状态
- [x] 关闭 #4
- [ ] 进入 #5 Plan View

## 2026-08-03 会话 1：领域建模 + 项目初始化

### 创建的文件

1. **`CONTEXT.md`** — 领域模型文档
   - 20 个核心领域词汇定义
   - 40 条已决领域决策
   - 8 个开放分叉（已全部关闭）
   - 分期规划（一/二/三期）

2. **`PROJECT.md`** — 项目框架文档
   - 技术栈：Spring Boot 3.5.16 + Java 21 + MyBatis + PostgreSQL + Vue 3.5.40
   - 项目结构（backend + frontend 完整目录树）
   - 数据库设计（8 张主要表结构）
   - RESTful API 设计
   - 开发路线图（4 个垂直切片）

3. **`PROGRESS.md`** — 进度追踪文档
   - 总体进度（领域建模完成 ✅）
   - 当前冲刺任务列表
   - 4 个里程碑计划
   - 会话记录

4. **`CHANGELOG.md`** — 本文件
   - 文件修改记录模板

### 修改的文件

*无（首次初始化）*

### 关键决策

- **技术栈锁定**：
  - 后端：Spring Boot 3.5.16 + Java 21 + MyBatis（非 JPA）
  - 数据库：PostgreSQL（关系型 + JSONB）
  - 前端：Vue 3.5.40 + Vite 8.2.0 + Element Plus 2.14.3
  - 部署：Docker Compose（开发）+ Render/Railway（生产）

- **开发策略**：垂直切片（4 个切片，每个切片包含数据层 → API → 前端）

- **领域建模完成**：
  - F.1 ~ F.8 所有开放分叉已关闭
  - 40 条决策已落 CONTEXT.md
  - TrainingDayFocus 枚举确定为 4 个（Push/Pull/Legs/FullBody）
  - FullBody 编排规则：轮换式全身 + 强制含 1 个 Core
  - Core 附属规则：按 Focus 类型选择性加

### 下一步行动

**切片 1：数据导入 + Exercise 详情**
1. 初始化 Spring Boot 项目（pom.xml）
2. 配置 MyBatis + PostgreSQL
3. 编写 docker-compose.yml
4. 设计 exercises 表结构（schema.sql）
5. 下载 exercises-dataset
6. 写数据导入脚本
7. 实现 ExerciseMapper + Service + Controller
8. 初始化 Vue 3.5.40 前端
9. 实现 Exercise 详情页

---

## 2026-08-03 会话 2：版本锁定 + 切片 1 初始化推进

### 创建的文件

1. **`.java-version`** — Java 版本锁定为 21.0.7
2. **`backend/src/main/java/com/fitness/dto/ApiResponse.java`** — 统一响应 Record
3. **`backend/src/main/java/com/fitness/exception/ErrorCode.java`** — 统一错误码
4. **`backend/src/main/java/com/fitness/exception/BusinessException.java`** — 业务异常封装
5. **`backend/src/main/java/com/fitness/exception/GlobalExceptionHandler.java`** — 全局异常处理器
6. **`frontend/`** — Vue 3.5.40 + Vite 8.2.0 前端骨架
7. **`backend/src/main/java/com/fitness/config/ExerciseSeedImporter.java`** — Exercise 数据导入器
8. **`backend/src/main/resources/data/exercises-dataset/exercises.json`** — exercises-dataset 主数据文件
9. **`backend/src/main/resources/migration/20260803_align_context_schema.sql`** — 本地旧 schema 对齐迁移
10. **`.gitignore`** — 忽略构建产物和依赖目录

### 修改的文件

- **`backend/pom.xml`** — Spring Boot 升级到 3.5.16，Java 升级到 21，MyBatis Starter 升级到 3.0.5
- **`backend/src/main/resources/application.yml`** — 数据源对齐 local-middleware，开启 Virtual Threads，降低 MyBatis JSONB 日志噪音
- **`backend/src/main/resources/schema.sql`** — 对齐 CONTEXT.md 的 Goal / PlanStatus / LoadType / ExerciseFeedback 决策
- **`backend/src/main/java/com/fitness/**`** — Exercise ID 改为保留数据集原始字符串 ID；Controller 改统一响应
- **`PROJECT.md` / `PROGRESS.md` / `backend/README.md`** — 写明 Java/Vue 精确版本并更新进度

### 关键决策

- Java 固定为 **21 LTS**，项目目标版本写入 `.java-version`
- 后端使用 **Spring Boot 3.5.16**，保留 3.x 主线，不跳到 Spring Boot 4.x
- Vue 固定为 **3.5.40**，前端依赖不使用宽泛 `latest`
- Exercise ID 保留数据集原始 ID，数据库使用 `VARCHAR(64)`，方便导入 1,324 条 Exercise 数据
- 数据集媒体路径转换为 GitHub raw URL，前端详情页可直接展示图片/GIF

### 下一步行动

- [x] 下载 exercises-dataset 数据
- [x] 编写数据导入器
- [x] 跑通 `GET /api/exercises/{id}` 与 Vue/Vite 代理联调
- [x] 为 Exercise API 补测试
- [ ] 前端页面视觉走查

---

## 2026-08-03 会话 3：切片 1 测试收口

### 创建的文件

- **`backend/src/test/java/com/fitness/controller/ExerciseControllerTest.java`** — Exercise API MVC slice 测试

### 修改的文件

- **`backend/src/main/java/com/fitness/FitnessApplication.java`** — 移除启动类 `@MapperScan`，避免 Web slice 测试误加载 MyBatis Mapper FactoryBean
- **`.gitignore`** — 忽略前端测试报告目录
- **`PROGRESS.md`** — 修正项目初始化和切片 1 状态

### 关键决策

- Controller 测试只验证 Web 合约，使用 `@WebMvcTest` + mocked `ExerciseService`，不连接数据库。
- Mapper 接口保留 `@Mapper` 注解，由 MyBatis Boot Starter 注册，启动类不再承担 Mapper 扫描职责。

### 下一步行动

- [ ] 前端详情页视觉走查与交互微调
- [ ] 切片 1 关账，进入切片 2 UserProfile

## 2026-08-03 会话 4：Matt skills 配置 + MVP PRD 草稿

### 创建的文件

- **`AGENTS.md`** — Agent skills 配置，中英双语
- **`docs/agents/issue-tracker.md`** — GitHub Issues 配置，中英双语
- **`docs/agents/triage-labels.md`** — 默认 triage labels 映射，中英双语
- **`docs/agents/domain.md`** — single-context domain docs 配置，中英双语
- **`docs/adr/.gitkeep`** — ADR 目录占位
- **`docs/prd/fitness-app-mvp-prd.md`** — MVP PRD 本地草稿

### 修改的文件

- **`PROGRESS.md`** — 增加 PRD/Issues 状态与会话 4 记录

### 关键决策

- Issue tracker 使用 GitHub Issues：`qiyee1688/fitness-app`
- Triage labels 使用默认值：`needs-triage` / `needs-info` / `ready-for-agent` / `ready-for-human` / `wontfix`
- Domain docs 使用 single-context：根目录 `CONTEXT.md` + `docs/adr/`
- PRD 测试 seam 确认为：后端 API seam、Service seam、Generator seam、前端 API/UI seam、数据库 seam

### 下一步行动

- [ ] 安装或配置 `gh` CLI
- [ ] 发布 `docs/prd/fitness-app-mvp-prd.md` 到 GitHub Issues 并打 `ready-for-agent`
- [ ] 运行 `/to-issues` 拆分 PRD

## 2026-08-03 会话 5：安装 gh + 发布 MVP PRD

### 创建的文件

*无*

### 修改的文件

- **`PROGRESS.md`** — 更新 PRD/Issues 状态，记录 GitHub Issue #1
- **`CHANGELOG.md`** — 记录 `gh` 安装、label 补齐与 PRD 发布

### 关键决策

- GitHub CLI 使用 Homebrew 安装，当前版本为 `gh 2.86.0`
- GitHub 登录账号确认为 `qiyee1688`
- 默认 triage labels 已同步到 GitHub 仓库：
  - `needs-triage`
  - `needs-info`
  - `ready-for-agent`
  - `ready-for-human`
  - `wontfix`（仓库默认已存在）
- MVP PRD 发布为 GitHub Issue：[#1 PRD: Fitness Coaching App MVP](https://github.com/qiyee1688/fitness-app/issues/1)

### 下一步行动

- [ ] 运行 `/to-issues` 拆分 PRD
- [ ] 拆分后更新 `PROGRESS.md` 与 `CHANGELOG.md`

## 2026-08-03 会话 6：拆分 PRD 为 GitHub Issues

### 创建的文件

*无*

### 修改的文件

- **`PROGRESS.md`** — 标记 `/to-issues` 完成，并增加 GitHub Issues 清单
- **`CHANGELOG.md`** — 记录 PRD issue 拆分结果

### 关键决策

- PRD 拆分采用 tracer-bullet vertical slices，每张 issue 都要求可独立验证或演示。
- 发布的 implementation issues：
  - [#2 Finish Exercise Detail Visual QA / 收口 Exercise 详情页视觉 QA](https://github.com/qiyee1688/fitness-app/issues/2)
  - [#3 UserProfile End-to-End / 用户档案端到端](https://github.com/qiyee1688/fitness-app/issues/3)
  - [#4 Plan Generator MVP / 8 周 Plan 生成器 MVP](https://github.com/qiyee1688/fitness-app/issues/4)
  - [#5 Plan View / Plan 查看页](https://github.com/qiyee1688/fitness-app/issues/5)
  - [#6 Today Workout Check-in / 今日 Workout 打卡](https://github.com/qiyee1688/fitness-app/issues/6)
  - [#7 ExerciseFeedback and HURT Substitution / ExerciseFeedback 与 HURT 替换](https://github.com/qiyee1688/fitness-app/issues/7)
  - [#8 Plan Lifecycle Automation / Plan 生命周期自动化](https://github.com/qiyee1688/fitness-app/issues/8)
- 所有 implementation issues 均已打 `ready-for-agent`。

### 下一步行动

- [ ] 优先处理 #2，完成 Exercise 详情页视觉 QA 后关闭切片 1
- [ ] 或从 #3 开始进入 UserProfile 端到端开发

## 2026-08-03 会话 7：整理 App 开发计划与功能说明

### 创建的文件

- **`docs/app-development-plan.md`** — App 开发计划与简单功能说明，汇总产品定位、MVP 功能、开发阶段和 issue 对应关系

### 修改的文件

- **`PROGRESS.md`** — 记录开发计划文档已整理完成
- **`CHANGELOG.md`** — 记录本次文档整理

### 关键决策

- 开发计划以一期 MVP 主路径为中心：`UserProfile -> Plan Generator -> 8 周 Plan -> Today Workout -> ExerciseFeedback`
- 推荐执行顺序保持与 GitHub Issues 一致：#2 到 #8
- #6 完成后 App 具备一期 MVP 核心体验，#7 和 #8 用于长期使用体验完善

### 下一步行动

- [ ] 优先处理 #2，完成 Exercise 详情页视觉 QA
- [ ] #2 完成后进入 #3 UserProfile 端到端

## 2026-08-03 会话 8：完成 Exercise 详情页视觉 QA

### 创建的文件

- **`backend/src/test/java/com/fitness/service/ExerciseServiceTest.java`** — 覆盖组合 BodyPart 筛选解析
- **`backend/src/test/resources/mockito-extensions/org.mockito.plugins.MockMaker`** — 切换 Mockito 测试 mock maker，避免当前 JDK 环境 self-attach 失败

### 修改的文件

- **`backend/src/main/java/com/fitness/mapper/ExerciseMapper.java`** — BodyPart 条件改为参数化列表
- **`backend/src/main/java/com/fitness/service/ExerciseService.java`** — 解析逗号分隔 BodyPart 组合筛选
- **`backend/src/main/resources/mapper/ExerciseMapper.xml`** — 使用 MyBatis `foreach` 生成参数化 `IN` 查询
- **`frontend/src/views/ExerciseList.vue`** — 增加 BodyPart tabs、空状态、媒体 fallback
- **`frontend/src/views/ExerciseDetail.vue`** — 补全 Exercise 元信息、步骤空状态、媒体 fallback
- **`frontend/src/styles.css`** — 调整列表、详情和媒体 fallback 样式
- **`PROGRESS.md`** — 标记切片 1 完成并记录验证结果
- **`CHANGELOG.md`** — 记录本次开发

### 关键决策

- UI 部位 Tab 按 `CONTEXT.md` 保持 5 个主入口：Chest、Back、Shoulders、Legs、Waist。
- Legs tab 映射为 `upper legs + lower legs`，由后端通过参数化 `IN` 查询支持。
- 远程 GitHub raw 图片可能加载慢或失败，前端媒体区域必须显示稳定 fallback 标签。

### 验证

- `mvn test` 通过：8 tests
- `npm run build` 通过
- Chrome headless 截图完成桌面列表、移动列表和详情页视觉 QA
- API 验证：`GET /api/exercises?page=1&pageSize=1&bodyPart=upper legs,lower legs` 返回 `total=286`
- GitHub Issue [#2](https://github.com/qiyee1688/fitness-app/issues/2) 已关闭

### 下一步行动

- [ ] 开始 #3 UserProfile End-to-End

## 2026-08-03 会话 9：Exercise 页面中英文展示与语言切换

### 创建的文件

- **`frontend/src/composables/useLanguage.js`** — 全局语言状态、持久化和页面文案
- **`frontend/src/utils/exerciseDisplay.js`** — Exercise 名称、标签和列表字段的展示翻译工具

### 修改的文件

- **`frontend/src/App.vue`** — 顶栏增加中文 / English 切换
- **`frontend/src/views/ExerciseList.vue`** — 列表标题、搜索、空状态、按钮、名称和标签接入语言展示
- **`frontend/src/views/ExerciseDetail.vue`** — 详情标题、字段名、标签、步骤内容接入语言展示
- **`frontend/src/styles.css`** — 增加语言切换样式，并调整移动端顶栏避免裁切
- **`PROGRESS.md`** — 记录本次中英文展示增强
- **`CHANGELOG.md`** — 记录本次开发

### 关键决策

- 不修改 Exercise 原始数据；中文展示在前端视图层完成。
- `instructionSteps` 按当前语言选择：中文用 `zh` 优先，英文用 `en` 优先。
- Exercise 名称没有完整中文字段，因此先用常见精确名称 + 词组/词级规则做中文展示，后续可替换为后端正式多语言字段。
- 支持 `?lang=zh` / `?lang=en` 初始化语言，方便截图和回归验证。

### 验证

- `mvn test` 通过：8 tests
- `npm run build` 通过
- Chrome headless 截图检查：
  - 中文桌面列表
  - 中文移动列表
  - 英文桌面列表
  - 英文详情页

### 下一步行动

- [ ] 开始 #3 UserProfile End-to-End

## 2026-08-03 会话 10：完成 UserProfile 端到端

### 创建的文件

- **`backend/src/main/java/com/fitness/domain/User.java`** — User 领域对象
- **`backend/src/main/java/com/fitness/domain/UserProfile.java`** — UserProfile 领域对象
- **`backend/src/main/java/com/fitness/domain/FitnessLevel.java`** — FitnessLevel 枚举
- **`backend/src/main/java/com/fitness/domain/Goal.java`** — Goal 枚举
- **`backend/src/main/java/com/fitness/dto/UserProfileRequest.java`** — UserProfile 请求 DTO 与校验约束
- **`backend/src/main/java/com/fitness/dto/UserProfileResponse.java`** — UserProfile 响应 DTO
- **`backend/src/main/java/com/fitness/mapper/UserMapper.java`** — User / UserProfile MyBatis Mapper 接口
- **`backend/src/main/java/com/fitness/service/UserService.java`** — UserProfile 创建、更新、读取业务逻辑
- **`backend/src/main/java/com/fitness/controller/UserController.java`** — UserProfile REST API
- **`backend/src/main/resources/mapper/UserMapper.xml`** — 参数化 SQL、UUID / enum / JSONB 映射
- **`backend/src/test/java/com/fitness/service/UserServiceTest.java`** — UserService 创建、更新、错误路径测试
- **`backend/src/test/java/com/fitness/controller/UserControllerTest.java`** — UserProfile Controller 统一响应与校验测试
- **`frontend/src/api/user.js`** — UserProfile 前端 API client
- **`frontend/src/views/UserProfile.vue`** — UserProfile 表单页

### 修改的文件

- **`backend/src/main/java/com/fitness/exception/ErrorCode.java`** — 增加 User / UserProfile 404 错误码
- **`frontend/src/api/http.js`** — 增加统一 `api_post`
- **`frontend/src/router/index.js`** — 增加 `/profile` 路由
- **`frontend/src/App.vue`** — 顶栏增加用户档案导航
- **`frontend/src/composables/useLanguage.js`** — 增加 UserProfile 中英文文案，并补齐中文页面纯中文展示
- **`frontend/src/styles.css`** — 增加 UserProfile 表单布局与设备选项样式
- **`docs/app-development-plan.md`** — 标记 #3 完成，并把下一优先级更新为 #4
- **`PROGRESS.md`** — 记录 #3 完成、验证结果和下一步
- **`CHANGELOG.md`** — 记录本次开发

### 关键决策

- 当前还没有完整登录系统，MVP 切片先使用本地 demo 用户：`demo / demo@fitness.local`。
- `POST /api/users/profile` 采用 upsert 风格：demo User 不存在时创建，UserProfile 不存在时插入，存在时更新。
- MyBatis 映射层保留参数化 SQL；PostgreSQL UUID、enum 和 JSONB 通过 XML 中显式 cast 处理。
- Java 领域对象 ID 暂用 `String`，避免 MyBatis UUID TypeHandler 在本地集成中产生额外复杂度。

### 验证

- `mvn test` 通过：15 tests
- `npm run build` 通过
- 真实 API 验证：
  - `GET /api/users/profile?username=demo` 返回统一成功响应
  - `POST /api/users/profile` 成功更新为 `INTERMEDIATE / MUSCLE_GAIN / daysPerWeek=4`
  - 再次 GET 确认 JSONB `availableEquipment=["body weight","dumbbell","barbell"]` 已持久化

### 下一步行动

- [ ] 关闭 GitHub Issue #3
- [ ] 开始 #4 Plan Generator MVP

## 2026-08-03 会话 11：补充开发服务脚本

### 创建的文件

- **`scripts/backend.sh`** — 后端服务启动、停止、重启、状态、日志、测试和 Exercise seed 入口
- **`scripts/frontend.sh`** — 前端服务启动、停止、重启、状态、日志和 build 入口
- **`scripts/middleware.sh`** — PostgreSQL / Redis Docker Compose 启停和日志入口
- **`scripts/dev.sh`** — 全栈开发入口，串联 middleware、backend、frontend

### 修改的文件

- **`.gitignore`** — 忽略 `.dev/` 运行时 pid/log 目录
- **`README.md`** — 增加开发服务脚本使用说明
- **`backend/README.md`** — 快速启动改为脚本入口
- **`local-middleware/README.md`** — 使用 `docker compose` 并修正宿主机端口为 `15432` / `16379`
- **`PROGRESS.md`** — 记录本次脚本补充
- **`CHANGELOG.md`** — 记录本次开发

### 关键决策

- 脚本运行状态写入项目根目录 `.dev/`，避免污染源码目录。
- 后端默认优先使用本机 Java 21.0.7 路径；如果开发者已设置 `JAVA_HOME`，尊重当前环境。
- 脚本只停止自己记录的 pid，不主动杀掉端口上的陌生进程，避免误伤开发者其他任务。

### 验证

- `bash -n scripts/backend.sh scripts/frontend.sh scripts/middleware.sh scripts/dev.sh` 通过
- `scripts/backend.sh test` 通过：15 tests
- `scripts/frontend.sh build` 通过

### 下一步行动

- [ ] 开始 #4 Plan Generator MVP

## 2026-08-03 会话 12：修复后端启动脚本失败

### 修改的文件

- **`scripts/backend.sh`** — 强制后端使用 Java 21+，补 Maven 路径兜底，启动时等待成功信号，失败时打印日志尾部
- **`scripts/frontend.sh`** — 后台启动改为 `nohup`，日志记录 Node / npm 版本
- **`README.md`** — 补充后端脚本使用固定 Java / Maven 的说明
- **`backend/README.md`** — 补充启动失败返回非 0 与日志尾部提示说明
- **`PROGRESS.md`** — 记录本次修复
- **`CHANGELOG.md`** — 记录本次修复

### 根因

- 后端脚本原先尊重已有 `JAVA_HOME`。如果开发者 shell 里 `JAVA_HOME` 指向 Java 8，Spring Boot Maven Plugin 3.5.16 会因 class file version 不兼容启动失败。

### 验证

- `bash -n scripts/backend.sh scripts/frontend.sh scripts/middleware.sh scripts/dev.sh` 通过
- `JAVA_HOME=/usr PATH=/usr/bin:/bin:/usr/sbin:/sbin scripts/backend.sh test` 通过：15 tests

### 下一步行动

- [ ] 开始 #4 Plan Generator MVP

---

## 修改记录模板（后续会话使用）

### 2026-XX-XX 会话 N：[任务名称]

#### 创建的文件
- `path/to/file.ext` — 简短说明

#### 修改的文件
- `path/to/file.ext` — 修改内容摘要

#### 关键决策
- 决策点 1
- 决策点 2

#### 下一步行动
- [ ] 待办 1
- [ ] 待办 2
