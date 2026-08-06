# PRD: Fitness Coaching App — 二期（用户自定 + NutritionTip）

## Problem Statement

一期 MVP 已完成训练闭环：UserProfile → Plan 生成 → Workout 打卡 → ExerciseFeedback。但有两个缺口：

1. **用户没有自由度**：所有训练都是系统生成的 8 周 Plan，用户不能按自己当天想练什么来生成一次 Workout，也不能保存自己喜欢的训练组合。
2. **没有饮食指导**：练前该吃什么、练后该补什么、全天营养怎么安排——这些信息完全缺失，练完不知道下一步该做什么。

二期目标：加入**按需生成 + 模板保存**的用户自定功能，同时加入 **NutritionTip 饮食附属**，让用户既能按当天心情选部位练，又能获得饮食建议。

## Solution

二期包含两大模块：

### 模块 A：用户自定（OnDemandWorkout + WorkoutTemplate + CoachCue）

用户选择身体部位 + 器械 → 系统按部位生成 3-6 个 Exercise 的 OnDemandWorkout，Prescription 按用户 FitnessLevel 调整。OnDemandWorkout 是统一 Workout 模型中来源为 `ON_DEMAND` 的训练，可保存为 WorkoutTemplate，也可直接开始。WorkoutTemplate 可复用或替换 Plan 中尚未执行的 Workout。每个 Exercise 附带 CoachCue（口语化教练提示）。

### 模块 B：NutritionTip（饮食附属）

在 Workout 详情页挂载 NutritionTip 卡片，每条 Workout 最多 3 条 Tip。NutritionTip 由 NutritionRule 半自动生成：编辑维护公式和无占位符的双语固定文案，系统按 UserProfile 计算 MacroTarget，并在 Workout 生成时保存规则、体重、计算结果和文案快照。

核心约束：
- 用户自定不替代 Plan，而是增强 Plan（可替换某天 Workout）
- Diet 不独立建上下文，所有饮食内容以 NutritionTip 形态出现
- CoachCue 挂在 Exercise 上，一期写 50 条热门 Exercise，二期补全
- MacroTarget 双轨：PER_KG_BODYWEIGHT 优先，ABSOLUTE fallback
- Note 禁止占位符，个性化由 MacroTarget 数值承载

## User Stories

### 用户自定

1. As a User, I want to select a body part and equipment, so that I can generate a workout for what I want to train today.
2. As a User, I want the generated workout to contain the right number of exercises for the selected body part, so that it feels like a complete session.
3. As a User, I want the prescription to match my fitness level, so that the workout is neither too easy nor too hard.
4. As a User, I want to see coaching cues on each exercise, so that I know how to perform it correctly.
5. As a User, I want to save a generated workout as a template, so that I can reuse it later.
6. As a User, I want to replace a day in my Plan with my saved template, so that I can customize my training plan.
7. As a User, I want templates to preserve the full prescription, so that I don't need to reconfigure sets/reps each time.

### 饮食附属

8. As a User, I want to see nutrition tips on my Workout, so that I know what to eat for training.
9. As a User, I want a pre-workout tip, so that I fuel properly before exercise.
10. As a User, I want a post-workout tip, so that I recover after training.
11. As a User, I want tips to change based on my Goal, so that the advice is relevant.
12. As a User, I want macro targets to adjust to my body weight, so that the numbers are personalized.
13. As a User, I want tips written in a friendly, beginner-friendly tone, so that I feel encouraged.

## Functional Requirements

### 模块 A：用户自定

#### OnDemandWorkout 生成

- 候选按 `selectionPriority`、精确器械匹配、`exerciseId` 稳定排序；相同请求结果可复现
- “换一组”通过 `variation` 参数在同一候选池中稳定偏移，不使用不可复现的随机数
- 二期不根据行为数据自动计算热度

- 用户选择身体部位（BodyPart 枚举）和器械（Equipment 枚举）
- 系统从 Exercise 库中筛选匹配的 Exercise，优先选热门动作
- 生成数量固定为 Chest=4、Back=5、Shoulders=4、Legs=6、Waist=3
- 候选不足时不得重复 Exercise 或生成残缺 Workout；可用 ExerciseSubstitute 补齐，否则返回统一业务错误
- Prescription 按用户 FitnessLevel 调整：
  - BEGINNER → 3×12，RPE 7-8，loadType=BODYWEIGHT
  - INTERMEDIATE → 4×10，RPE 8，loadType 按 AvailableEquipment
  - ADVANCED → 5×5，RPE 8-9，loadType 按 AvailableEquipment
- 无 UserProfile 时使用 BEGINNER 默认参数
- UserProfile 的 AvailableEquipment 是默认器械；BODYWEIGHT 始终可选，用户可临时补充本次器械
- 临时器械只进入当前 Workout 生成快照；只有用户明确选择时才回写 UserProfile
- 生成时持久化为当前 User 所有的 `source=ON_DEMAND, status=DRAFT` Workout，并生成 Prescription 与 NutritionTip 快照
- 草稿开始训练后进入 `IN_PROGRESS`，完成后进入 `COMPLETED`
- 超过 24 小时且从未开始、未保存为模板的 DRAFT 可清理，训练历史不可自动删除

#### CoachCue

- 每个 Exercise 可有一个 `coachCue` 字段（TEXT, nullable）
- 由编辑撰写，所有用户看到相同内容
- 例："沉肩、收紧核心、不要弓背"
- 支持中英文（coachCue / coachCueEn）
- 一期只写前 50 个热门 Exercise，二期补全

#### WorkoutTemplate

- 用户保存 OnDemandWorkout 为模板
- 模板快照保存 `exerciseId / sequence / sets / reps / load / loadType / rpe`；Exercise 展示内容和 CoachCue 实时引用当前 Exercise
- 模板列表可查看、删除，并可轻量编辑名称、顺序、Prescription 和系统提供的 Substitute
- 模板复用默认严格保留 Prescription，不按当前 UserProfile 自动重算
- Exercise 停用时先尝试 Substitute；无法修复或负荷类型不兼容时标记为“需要修复”，禁止使用
- 用户可将模板替换到 Plan 中当前或未来、尚未完成且当前生效的 Workout
- 替换在单一事务中使用 Plan 乐观锁；新 Workout 引用原 Workout，原记录标记 `REPLACED` 并保留历史
- 再次更换时替换当前生效的 replacement，形成可追溯替换链；日期和 dayNumber 不变

#### API

```
POST /api/workouts/on-demand           # 生成 OnDemandWorkout
POST /api/workouts/{workoutId}/start   # 开始按需训练
POST /api/workout-templates            # 保存为模板
GET  /api/workout-templates            # 查看所有模板
PATCH /api/workout-templates/{id}      # 轻量编辑模板（乐观锁）
DELETE /api/workout-templates/{id}     # 删除模板
POST /api/plans/{planId}/workouts/{workoutId}/replace  # 用模板替换某天 Workout
```

### 模块 B：NutritionTip

#### NutritionTip 实体

- 每条 NutritionTip 关联一个 Workout，关系为 1:N
- 单条 Workout 最多 3 条 Tip，对应 3 种 NutritionTiming 各 1 条
- 字段：`macroTargets` + `timing` + `note/noteEn` + `ruleId/ruleVersion` + `weightKgSnapshot`
- 生成时机：Plan 或 OnDemandWorkout 生成时，按 Goal、可选 Focus 和 Timing 匹配 NutritionRule
- 匹配时精确 Focus 优先，未命中则使用 `focus=NULL` 的通用 Rule；仍未命中则不生成 Tip
- Tip 是生成时快照，之后 UserProfile、体重或 NutritionRule 变化不修改历史数据

#### MacroTarget 结构

- `protein/carbs/fat/kcal: {value, unit: GRAMS | KILOCALORIES, basis: ABSOLUTE | PER_KG_BODYWEIGHT}`
- PER_KG_BODYWEIGHT 优先，ABSOLUTE 作为 fallback
- UserProfile 使用可空的 `weightKg`，合法范围 30.0-300.0 kg、最多一位小数；lb 仅在输入和展示层换算
- 缺少体重时使用 ABSOLUTE fallback；单项不可计算时省略该项，整条规则无可计算目标时不生成 Tip

#### NutritionRule 实体

- `condition: {goal, focus?, timing}`——不包含 level，focus 可空
- `formula: {protein, carbs, fat, kcal}` + `note/noteEn` + `version/enabled`
- 二期 MVP 覆盖 6 条通用 Rule：MUSCLE_GAIN/FAT_LOSS × `focus=NULL` × 3 Timing
- 同一 `goal + focus + timing` 只能有一条启用版本

#### API

```
GET  /api/workouts/{workoutId}/nutrition-tips    # 获取 Workout 的 NutritionTip 列表
```

### 身份与资源所有权

- Controller 不接收由客户端指定的 userId；Service 统一通过 CurrentUserProvider 获取当前用户
- 二期提供与现有 demo 用户机制兼容的开发环境实现，暂不实现完整 Sa-Token 登录
- 生产环境无法取得可靠身份时拒绝访问私有资源，不得回退到公共 demo 用户
- Workout、WorkoutTemplate 的查询、更新和删除必须同时校验资源 ID 与 ownerUserId
- 资源不存在与越权统一返回“资源不存在”，避免泄露其他用户资源
- 后续接入 Sa-Token 时替换 CurrentUserProvider 实现，并独立使用 role/permission 管理管理员权限

### 数据库迁移

- 扩展统一 `workouts` 模型：直接 owner、可空 plan、source、status、替换引用和生成快照；Plan Workout 从 READY 开始
- 新增 `exercises.coach_cue` 和 `exercises.coach_cue_en` 字段
- 新增 `exercises.selection_priority` 字段；数值越小越优先，未配置使用统一默认值
- 新增 `nutrition_rules` 表
- 新增 `nutrition_tips` 表
- 新增 `workout_templates` 表
- 新增 `workout_templates_exercises` 关联表
- 新增 `user_profiles.weight_kg` 字段
- 预置 50 条热门 Exercise 的 CoachCue 数据
- 通过幂等数据库迁移预置 6 条核心 NutritionRule；不暴露运行时 seed API

### 前端

- 首页新增"自定训练"入口
- 部位 + 器械选择 → 生成 OnDemandWorkout 展示
- 保存为模板按钮
- 模板列表管理页
- 模板轻量编辑和“需要修复”状态
- Plan View 中模板替换入口
- Exercise 详情页展示 CoachCue
- Workout 详情/Today 页展示 NutritionTip 卡片
- 中英文切换覆盖所有新增字段

## Out of Scope (二期不做)

- Sa-Token 用户、管理员与后台配置权限体系 → 后续阶段统一建设；二期不得临时暴露无鉴权的管理接口
- NutritionRule 后台编辑与发布界面 → 待 Sa-Token 管理员权限体系完成后建设

- 食物换算 → 三期 KnowledgeArticle
- 独立饮食上下文 → 不做
- Focus 精确覆盖 Rule（只做 6 条通用核心 Rule）
- 模板替换 Plan 时的"追加"模式（只做替换）
- CoachCue 的个性化变体（所有用户看到相同内容）
- 用户自定义饮食目标

## Acceptance Criteria

### 用户自定
1. 选择"练胸 + 哑铃" → 生成 4 个 Exercise 的 Workout
2. BEGINNER 用户看到 3×12@RPE 7-8 的 Prescription
3. Exercise 详情页展示 CoachCue
4. 保存 Workout 为模板后，模板列表可见
5. 用模板替换 Plan 中某天 Workout 后，当天显示自定义内容
6. 已完成或过去日期的 Workout 不可替换，重复替换不会产生两个当前生效 Workout
7. 模板复用保持保存时 Prescription，停用 Exercise 无法替代时进入“需要修复”

### NutritionTip
8. 增肌 Goal 的 Workout 显示增肌 Tip，减脂 Goal 显示减脂 Tip
9. 每条 Workout 最多 3 条 Tip（PRE/POST/DAILY 各 1）
10. 体重 60kg 时 PER_KG 数值按 60kg 计算；无体重时使用 ABSOLUTE fallback
11. Note 为 Rule 提供的完整双语句子，不含占位符，并随 Tip 保存快照
12. 中英文切换正确
13. 无匹配 Rule 时不显示 Tip（不报错）
14. 修改体重或 Rule 不改变历史 Workout 的 NutritionTip

## Delivery Plan

### 2A：Workout 基础模型与按需训练

对应 Issue：[二期 2A：生成并开始用户私有的 OnDemandWorkout](https://github.com/qiyee1688/fitness-app/issues/22)、[二期 2A：提供确定性换组与双语 CoachCue](https://github.com/qiyee1688/fitness-app/issues/23)

- 统一 Workout 所有权、来源、状态、替换引用和生成快照
- 建立 CurrentUserProvider 身份边界
- 完成 CoachCue、selectionPriority、确定性生成、variation、草稿清理及前端按需训练闭环

### 2B：WorkoutTemplate 与 Plan 替换

对应 Issue：[二期 2B：保存并管理 WorkoutTemplate](https://github.com/qiyee1688/fitness-app/issues/24)、[二期 2B：编辑和修复 WorkoutTemplate](https://github.com/qiyee1688/fitness-app/issues/25)、[二期 2B：使用 WorkoutTemplate 替换 Plan Workout](https://github.com/qiyee1688/fitness-app/issues/26)

- 完成模板保存、列表、删除、轻量编辑和“需要修复”状态
- 完成器械校验、ExerciseSubstitute 修复及 Plan 乐观锁替换链
- 完成模板管理与 Plan 替换前端闭环

### 2C：NutritionTip

对应 Issue：[二期 2C：为 Plan Workout 生成个性化 NutritionTip](https://github.com/qiyee1688/fitness-app/issues/27)、[二期 2C：让 NutritionTip 覆盖按需与模板替换 Workout](https://github.com/qiyee1688/fitness-app/issues/28)

- 完成 weightKg、NutritionRule 迁移 seed、MacroTarget 计算和 ABSOLUTE fallback
- 完成 NutritionTip 生成时快照、Today/Workout 饮食卡片与双语内容
