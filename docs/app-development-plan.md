# Fitness App 开发计划与功能说明

最后更新：2026-08-25

## 0. 当前 Issue 路线图

GitHub Issue 是交付追踪的来源；本表同步“代码已合入”和“GitHub 已关闭”这两个不同状态。已合入的 Issue 仍需按各自验收条件复核、在 Issue 留下交付说明后再关闭。

| 阶段 | GitHub Issue | 计划归类 | 当前处理 |
| --- | --- | --- | --- |
| 一期 MVP | #2–#8 | 核心训练闭环 | 已完成并关闭。 |
| 二期 PRD | #13 | 用户自定训练与 NutritionTip 的父 PRD | 已由 #22–#28 交付切片覆盖；待归档关闭。 |
| 二期 2A | #22、#23 | 按需训练、确定性换组与 CoachCue | 功能已合入；待逐项验收归档。 |
| 二期 2B | #24、#25、#26 | WorkoutTemplate 保存、编辑修复与 Plan 替换 | 功能已合入；待逐项验收归档。 |
| 二期 2C | #27、#28 | NutritionTip 生成及其对按需/替换训练的覆盖 | 功能已合入；待逐项验收归档。 |
| 三期 PRD | #34 | KnowledgeArticle 与 FoodItem 的父 PRD | 已由 #35–#39 交付切片覆盖；待归档关闭。 |
| 三期 3A–3E | #35、#36、#37、#38、#39 | 官方科普内容、食物目录、份量换算与内容入口 | 功能已合入；#37–#39 已在本轮完整回归，全部待逐项验收归档。 |
| 四期 PRD | #40 | 基于反馈的可解释处方微调父 PRD | 已由 #41–#43 交付切片覆盖；待归档关闭。 |
| 四期 4A | #41、#42、#43 | Adjustment 候选、确认/拒绝 API 与双语界面 | 功能已合入并完成后端/前端回归；待逐项验收归档。 |

### 排期与暂缓规则

- 当前开放 Issue 均在路线图中：没有遗漏到计划外的开放 Issue，也没有与产品约束冲突而需要暂缓的 Issue。
- 下一项工作不是扩展新范围，而是对 #13、#22–#28、#34–#43 进行按验收条件的归档核对，然后关闭已完成的子 Issue，最后关闭对应父 PRD。
- 继续遵守既定非目标：独立 Diet 上下文、用户自定义食品、外部食物数据同步、运行时食品编辑后台、社交/付费/可穿戴集成均不进入当前路线图。

## 1. 产品定位

Fitness App 是面向健身新手的指导型训练应用。它不是单纯的 Exercise 百科，而是帮助用户回答三个问题：

- 今天该练什么？
- 每个 Exercise 应该怎么练？
- 练完以后下一步如何调整？

一期 MVP 的主路径是：

```text
UserProfile -> Plan Generator -> 8 周 Plan -> Today Workout -> ExerciseFeedback
```

Exercise 浏览和详情页作为辅助入口，用来查看 GIF、图片、Target、Equipment 和 instruction_steps。

## 2. 一期 MVP 功能范围

| 模块 | 简单说明 | 用户价值 | GitHub Issue |
| --- | --- | --- | --- |
| Exercise 浏览与详情 | 查看、搜索、筛选 Exercise，并打开详情页看媒体和步骤 | 不懂动作时能快速确认练法 | [#2](https://github.com/qiyee1688/fitness-app/issues/2) |
| UserProfile | 录入 FitnessLevel、Goal、daysPerWeek、AvailableEquipment | 让系统知道用户水平、目标、时间和可用 Equipment | [#3](https://github.com/qiyee1688/fitness-app/issues/3) |
| Plan Generator | 根据 UserProfile 生成默认 8 周 Plan | 用户不用自己编训练计划 | [#4](https://github.com/qiyee1688/fitness-app/issues/4) |
| Plan View | 展示 ACTIVE Plan 的 8 周 Workout 安排 | 用户能看懂未来训练路径 | [#5](https://github.com/qiyee1688/fitness-app/issues/5) |
| Today Workout | 展示今天的 Workout，并支持完成打卡 | 用户打开 App 就能开始训练 | [#6](https://github.com/qiyee1688/fitness-app/issues/6) |
| ExerciseFeedback | 对单个 Exercise 提交 TOO_EASY、JUST_RIGHT、TOO_HARD、HURT 反馈 | 让训练后续能更安全、更合适 | [#7](https://github.com/qiyee1688/fitness-app/issues/7) |
| Plan 生命周期 | 管理 ACTIVE、SCHEDULED、PAUSED、COMPLETED、SUPERSEDED、CANCELLED 等状态 | 避免多个当前计划混乱，保留历史 | [#8](https://github.com/qiyee1688/fitness-app/issues/8) |

## 3. 当前状态

已完成：

- 领域模型已锁定，详见 `CONTEXT.md`。
- 技术栈已锁定：Java 21、Spring Boot 3.5.16、Vue 3.5.40、Vite 8.2.0。
- 后端基础工程、MyBatis、PostgreSQL、Redis 本地中间件已配置。
- Exercise 数据已导入本地 PostgreSQL，共 1,324 条。
- Exercise API 与前端列表/详情骨架已完成。
- Exercise Controller 测试已补充。
- UserProfile API、持久化和前端档案表单已完成。
- 页面支持中文 / English 语言切换，中文页面名称、内容和标签已中文化。
- MVP PRD 已发布为 [#1](https://github.com/qiyee1688/fitness-app/issues/1)。
- Implementation issues 已拆分为 [#2](https://github.com/qiyee1688/fitness-app/issues/2) 到 [#8](https://github.com/qiyee1688/fitness-app/issues/8)。
- Plan Generator、Plan View 与 Today Workout 已完成：可查看 8 周安排、打开当天 Workout、完成幂等打卡并跳转 Exercise 详情。
- ExerciseFeedback 已完成：支持四类反馈，HURT 会记录身体部位、立即替换或安全移除当前动作，并保存未来 4 周过滤状态。
- Plan 生命周期自动化已完成：支持未来排队、超时 PAUSED/CANCELLED、到期 child Plan 自动续期和乐观锁迁移。

当前优先级：

1. 按 Issue 验收条件完成 #13、#22–#28、#34–#43 的归档核对与关闭。
2. 归档完成前不新增新的产品功能范围。

## 4. 开发阶段计划

### 阶段 0：基础建设，已完成

目标：让项目具备后续垂直切片开发的基本骨架。

交付物：

- Spring Boot 后端工程。
- Vue 前端工程。
- PostgreSQL + Redis 本地开发环境。
- 统一 API 响应：`{code, message, data}`。
- 全局异常处理器。
- Exercise 数据导入与查询能力。

完成标准：

- `GET /api/exercises` 和 `GET /api/exercises/{id}` 可用。
- Exercise 列表和详情页可以通过 Vite 代理访问后端。
- 后端 Controller 测试覆盖核心成功和错误路径。

### 阶段 1：Exercise 浏览收口

对应 issue：[#2](https://github.com/qiyee1688/fitness-app/issues/2)

目标：把现有 Exercise 列表和详情页从“能用”收口到“体验完整”。

核心功能：

- Exercise 列表、搜索、筛选、分页。
- Exercise 详情展示图片/GIF、BodyPart、Target、Equipment、muscle_group、instruction_steps。
- 加载、空数据、404、API 错误状态。

完成标准：

- 桌面和移动端视觉走查通过。
- `npm run build` 通过。
- 如果改动后端 API，`mvn test` 通过。

### 阶段 2：UserProfile 端到端

对应 issue：[#3](https://github.com/qiyee1688/fitness-app/issues/3)

状态：已完成。

目标：用户可以填写训练档案，作为生成 Plan 的输入。

核心功能：

- 创建或更新 UserProfile。
- 字段包括 FitnessLevel、Goal、daysPerWeek、AvailableEquipment。
- 前端提供 UserProfile 表单。
- 后端保存 UserProfile，并支持获取当前档案。

完成标准：

- Controller 只做参数校验和响应包装。
- 业务逻辑在 Service 层。
- Mapper SQL 参数化，不拼接 SQL。
- `mvn test` 和 `npm run build` 通过。
- 真实 API 验证可创建、更新并读取 UserProfile，`AvailableEquipment` 通过 PostgreSQL JSONB 持久化。

### 阶段 3：Plan Generator MVP

对应 issue：[#4](https://github.com/qiyee1688/fitness-app/issues/4)

目标：根据 UserProfile 生成默认 8 周训练计划。

核心功能：

- 生成 Plan、Workout、Prescription。
- Plan 保存 UserProfile 快照。
- Workout 使用 Push、Pull、Legs、FullBody。
- Core 不作为 TrainingDayFocus，只通过 Waist Exercise 附加。
- Prescription 包含 sets、reps、loadType、rpe。
- 同一 User 最多只有一个 ACTIVE Plan。
- 重新生成时旧 Plan 标记为 SUPERSEDED。

完成标准：

- Generator 能在无 Template 命中时降级生成，不出现空白 Plan。
- 生成逻辑可脱离 HTTP 做 Service/Generator 测试。
- ACTIVE Plan 切换明确使用乐观锁或 Redis 分布式锁策略。
- `mvn test` 通过。

### 阶段 4：Plan View（已完成）

对应 issue：[#5](https://github.com/qiyee1688/fitness-app/issues/5)

目标：用户可以查看当前 ACTIVE Plan 的 8 周安排。

核心功能：

- 展示 Plan 基本信息、周计划、Workout 列表。
- 展示每个 Workout 的 TrainingDayFocus。
- 展示 Prescription 的 Exercise、sets、reps、loadType、rpe。
- 从 Prescription 跳转到 Exercise 详情。

完成标准：

- 无 ACTIVE Plan、加载中、API 错误状态清晰。
- 页面能支撑后续 Today Workout 入口。
- `mvn test` 和 `npm run build` 通过。

### 阶段 5：Today Workout 打卡（已完成）

对应 issue：[#6](https://github.com/qiyee1688/fitness-app/issues/6)

目标：用户每天打开 App 就能看到今天该练什么，并完成打卡。

核心功能：

- 获取 ACTIVE Plan 中今天的 Workout。
- 展示线性 Prescription 列表。
- 完成 Workout 打卡。
- 保留 completed_at 或等价历史记录。

完成标准：

- 今日 Workout 选择逻辑有测试覆盖。
- 已完成、无 ACTIVE Plan、API 错误状态清晰。
- 打卡后 UI 自动更新。
- `mvn test` 和 `npm run build` 通过。

### 阶段 6：ExerciseFeedback 与 HURT 处理

对应 issue：[#7](https://github.com/qiyee1688/fitness-app/issues/7)

目标：用户能反馈每个 Exercise 的实际感受，并让 HURT 反馈影响后续训练。

核心功能：

- 支持 TOO_EASY、JUST_RIGHT、TOO_HARD、HURT_<body_part>。
- 保存 ExerciseFeedback。
- HURT 对当前 Workout 立即替换或安全降级。
- HURT 对未来 4 周同肌群或同 Exercise 过滤。

完成标准：

- FeedbackEffect 规则在 Service 层实现。
- HURT 分支有测试覆盖。
- 无替代 Exercise 时有明确 fallback。
- `mvn test` 和 `npm run build` 通过。

### 阶段 7：Plan 生命周期自动化

对应 issue：[#8](https://github.com/qiyee1688/fitness-app/issues/8)

目标：让 Plan 的状态变化符合长期使用场景。

核心功能：

- 支持 DRAFT、SCHEDULED、ACTIVE、PAUSED、COMPLETED、SUPERSEDED、CANCELLED。
- 支持未来 SCHEDULED Plan。
- 8 周完成后可自动续期，生成 child Plan。
- 连续未打卡后 PAUSED，再长期未恢复后 CANCELLED。

完成标准：

- PlanStatus 状态迁移有 Service 测试。
- 并发敏感状态变更明确使用乐观锁或 Redis 分布式锁。
- 不删除历史 Plan。
- `mvn test` 通过。

## 5. 当前暂不做的功能

以下内容不在当前已排期 Issue 范围内：

- 独立 Diet 上下文、用户自定义食品、外部食物数据同步与食谱生成。
- Sa-Token 身份/管理员体系，以及运行时 NutritionRule 或 FoodItem 编辑后台。
- 用户生成内容。
- 完整 ExerciseSubstitute 编辑后台。
- Circuit 或 Superset WorkoutBlock。
- 生产部署自动化。
- 付费、社交、教练市场、可穿戴设备集成。

## 6. 技术与质量约束

后端：

- Java 21+。
- Spring Boot 3.5.16。
- MyBatis XML Mapper。
- PostgreSQL JSONB 用于结构化快照和数据集字段。
- Redis 用于后续分布式锁场景。
- Controller 只做参数校验和响应包装。
- 业务逻辑下沉到 Service 层。
- 所有错误返回统一 JSON：`{code, message, data}`。
- 删除、批量更新前需要终端确认信息。
- SQL 必须参数化，禁止字符串拼接。

前端：

- Vue 3.5.40。
- Vite 8.2.0。
- Element Plus 2.14.3。
- 页面必须有加载、空数据和错误状态。
- Exercise、Plan、Workout 相关页面都需要移动端和桌面端视觉检查。

验证：

- Java 修改后运行 `mvn test`。
- 前端修改后运行 `npm run build`。
- 视觉相关修改需要实际页面走查。

## 7. 推荐执行顺序

1. 完成 #2，关闭切片 1。
2. 完成 #3，建立 UserProfile 输入。
3. 完成 #4，让系统能生成 Plan。
4. 完成 #5，让用户能看 Plan。
5. 完成 #6，让用户能完成今日 Workout。
6. 完成 #7，让反馈影响训练。
7. 完成 #8，让 Plan 生命周期可持续运行。

做到 #6 后，App 已经具备一期 MVP 的核心体验；#7 和 #8 用来把体验从“能跑”推进到“能长期使用”。
