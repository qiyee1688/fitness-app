# Fitness App 项目完成度统计

**统计日期**: 2026-08-11  
**当前分支**: codex/issue-23-coach-cues

---

## 一、项目概览

### 技术栈
- **后端**: Java + Spring Boot + MyBatis + PostgreSQL
- **前端**: Vue.js 3
- **代码规模**: 
  - Java 文件: 76 个
  - 前端文件: ~3,890 个（含 node_modules）
  - 数据库迁移: 8 个 SQL 文件

### 核心组件
- **Controller 层**: 5 个（Exercise, User, Plan, Workout, WorkoutTemplate）
- **Service 层**: 5 个
- **领域模型**: 19 个核心实体
- **测试覆盖**: 12 个测试文件

---

## 二、一期完成情况（✅ 100%）

### 核心功能已完成
✅ **用户档案管理** (UserProfile)
- FitnessLevel / Goal / DaysPerWeek / AvailableEquipment
- 体重字段（weightKg，支持 kg 和 lb 换算）

✅ **计划生成** (Plan)
- 骨架模板 + 生成器降级
- Plan 状态机 7 态：DRAFT / SCHEDULED / ACTIVE / PAUSED / COMPLETED / SUPERSEDED / CANCELLED
- Plan 生命周期自动化（续期、中途放弃软降级）

✅ **训练执行** (Workout)
- Workout 打卡流程
- WorkoutBlock 线性序列（MVP 不支持循环/复合组）
- Prescription 处方（sets/reps/load/loadType/rpe）

✅ **Exercise 百科**
- 1,324 条 Exercise 数据导入
- 多语言步骤 / GIF / 图片
- 5 个部位 Tab：练胸/练背/练肩/练腿/练核心

✅ **反馈机制** (ExerciseFeedback)
- Exercise 级粒度反馈（TOO_EASY/JUST_RIGHT/TOO_HARD/HURT）
- HURT 双层生效：当下换动作 + 未来 4 周过滤

✅ **友好模式**
- 子集匹配 + ExerciseSubstitute 自动找替代
- 混合模式：默认自动换 1 个 + 手动切换列表

**已合并 PR**:
- #12: MVP integration QA
- #11: Plan lifecycle automation
- #10: Exercise feedback and HURT substitution
- #9: Plan workflow and today workout check-in
- #29: Phase two planning

---

## 三、二期进行中（🟡 约 60%）

### 二期目标
1. **模块 A**: 用户自定（OnDemandWorkout + WorkoutTemplate + CoachCue）
2. **模块 B**: NutritionTip（饮食附属）

### 已完成部分（✅）

#### 数据库迁移 ✅
- `20260806_add_coach_cues.sql` - CoachCue 字段 + 50 个热门 Exercise 优先级
- `20260806_add_on_demand_workouts.sql` - Workout 扩展（source/status/owner_user_id）
- `20260806_add_workout_templates.sql` - WorkoutTemplate + WorkoutTemplateExercise 表
- `20260806_add_workout_replacement_chain.sql` - 替换链追溯

#### 后端领域模型 ✅
- OnDemandBodyPart 枚举（CHEST/BACK/SHOULDERS/LEGS/WAIST）
- WorkoutSource 枚举（PLAN_GENERATED/ON_DEMAND/TEMPLATE_REPLACEMENT）
- WorkoutStatus 枚举（DRAFT/READY/IN_PROGRESS/COMPLETED/REPLACED）
- WorkoutTemplate / WorkoutTemplateExercise / WorkoutTemplateStatus 实体

#### 已关闭 Issues ✅
- #14-21: 二期基础设施（数据库迁移、后端 API、前端页面）

### 进行中功能（🟡）

#### Issue #22: 生成 OnDemandWorkout
- **状态**: OPEN, ready-for-agent
- **描述**: 用户选择身体部位 + 器械 → 生成 4-6 个 Exercise
- **依赖**: 数据库已就绪

#### Issue #23: 确定性换组与双语 CoachCue（当前分支）
- **状态**: OPEN, ready-for-agent
- **当前分支**: codex/issue-23-coach-cues
- **描述**: Exercise 级变化 + CoachCue 双语展示
- **相关文件修改**:
  - PlanDetailResponse.java / PlanService.java（已修改）
  - PlanView.vue / useLanguage.js（已修改）
  - 20260806_add_workout_templates.sql（已修改）

#### Issue #24: 保存并管理 WorkoutTemplate
- **状态**: OPEN, ready-for-agent
- **描述**: 保存 OnDemandWorkout 为模板 + 列表查看删除

#### Issue #25: 编辑和修复 WorkoutTemplate
- **状态**: OPEN, ready-for-agent
- **描述**: 修改模板名称、Exercise 顺序、Prescription

#### Issue #26: 使用 WorkoutTemplate 替换 Plan Workout
- **状态**: OPEN, ready-for-agent
- **描述**: 模板替换 Plan 中某天 Workout + 保留原记录

### 未开始功能（❌）

#### Issue #27: 为 Plan Workout 生成 NutritionTip
- **状态**: OPEN, ready-for-agent, **BLOCKED BY #26**
- **描述**: NutritionRule seed + MacroTarget 计算 + 双语文案

#### Issue #28: NutritionTip 覆盖按需与模板 Workout
- **状态**: OPEN, ready-for-agent
- **描述**: OnDemandWorkout 和 TEMPLATE_REPLACEMENT 也生成 NutritionTip

**待开发**:
- NutritionTip 实体（数据库迁移缺失）
- NutritionRule 实体（数据库迁移缺失）
- MacroTarget 结构化字段
- NutritionTiming 枚举（PRE_WORKOUT/POST_WORKOUT/DAILY）
- 6 条核心 Rule seed（增肌/减脂 × 3 Timing）
- NutritionService 和相关 API

---

## 四、开放分叉状态

根据 `fitness-app-open-forks.md`，只剩 2 个未决问题：

- **F.1 数据归一化**: 决策 31-32 已锁定为"不归一"，保持 1,324 条原样 ✅
- **F.4 FullBody 编排规则**: 决策 37-40 已锁定（轮换式全身 + 强制 1 个 Core） ✅

**所有开放分叉已关闭** ✅

---

## 五、二期完成度细分

### 模块 A: 用户自定（🟡 50%）

| 功能点 | 状态 | Issue |
|--------|------|-------|
| 数据库 schema | ✅ | - |
| OnDemandWorkout 生成 | 🟡 进行中 | #22 |
| CoachCue 双语展示 | 🟡 进行中 | #23 |
| WorkoutTemplate 保存 | ❌ 未开始 | #24 |
| WorkoutTemplate 编辑 | ❌ 未开始 | #25 |
| 模板替换 Plan Workout | ❌ 未开始 | #26 |

### 模块 B: NutritionTip（❌ 0%）

| 功能点 | 状态 | Issue |
|--------|------|-------|
| 数据库 schema | ❌ 未创建 | - |
| NutritionRule seed | ❌ 未创建 | #27 |
| Plan Workout 生成 Tip | ❌ 未开始 | #27 |
| 按需/模板 Workout 生成 Tip | ❌ 未开始 | #28 |
| Today + Plan 详情展示 Tip | ❌ 未开始 | #27 |

**二期整体完成度**: 约 **25%**（仅完成基础设施，核心功能未实现）

---

## 六、后续规划建议

### 短期（1-2 周）
1. **完成 Issue #22-23**（OnDemandWorkout + CoachCue）
   - 当前分支已有进度，优先合并
2. **完成 Issue #24-26**（WorkoutTemplate 全流程）
   - 模块 A 收尾

### 中期（2-4 周）
3. **创建 NutritionTip 数据库迁移**
   - `nutrition_rules` 表（condition/formula/note/noteEn/version/enabled）
   - `nutrition_tips` 表（workoutId/timing/macroTargets JSONB/note/noteEn/ruleId/ruleVersion/weightKgSnapshot）
   - `nutrition_timing_enum`（PRE_WORKOUT/POST_WORKOUT/DAILY）

4. **实现 Issue #27**（Plan Workout NutritionTip）
   - NutritionRule seed 数据（6 条）
   - MacroTarget 计算逻辑（PER_KG_BODYWEIGHT 优先）
   - NutritionService + API

5. **实现 Issue #28**（OnDemand/Template NutritionTip）
   - 扩展生成逻辑到非 Plan Workout

### 长期（三期）
6. **KnowledgeArticle + 食物换算**
   - 科普文章（Editor 撰写，UGC 口吻）
   - 食物库 + 宏量素换算（"4 个蛋"）

---

## 七、技术债务

1. **测试覆盖不足**: 只有 12 个测试文件，主要集中在 Controller 层
2. **WorkoutTemplate schema 问题**: 
   - 存在 `codex/workout-template-schema-fix` 分支和 PR #33
   - 需要检查并合并修复
3. **NutritionTip 完全缺失**: 数据库表、Service、API 都未创建
4. **前端视图不完整**: 
   - OnDemandWorkout.vue 存在但可能未完成
   - WorkoutTemplates.vue 存在但功能未实现
   - NutritionTip 卡片未创建

---

## 八、关键决策记录（CONTEXT.md）

项目已有 **58 条已决领域决策**，核心约束：
- Exercise 难度在 Prescription 里，不在 Exercise 上
- Diet 附属于 Workout，无独立上下文
- NutritionTip note 禁止占位符，UGC 轻松口语风格
- MacroTarget 双轨：PER_KG_BODYWEIGHT 优先 + ABSOLUTE fallback
- 二期 MVP 只做 6 条核心 Rule（增肌/减脂 × 3 Timing）

---

## 九、总结

### 整体完成度
- **一期（MVP）**: ✅ **100%** - 已上线可用
- **二期（用户自定 + NutritionTip）**: 🟡 **约 25%** - 基础设施就绪，核心功能未实现
- **三期（KnowledgeArticle）**: ❌ **0%** - 未开始

### 当前状态
- **可发布**: 一期功能完整，可作为 MVP 使用
- **二期瓶颈**: NutritionTip 模块完全空白，需要从数据库设计开始
- **技术债务**: 测试覆盖、schema 修复、前端视图完善

### 建议优先级
1. **立即**: 完成 Issue #22-23（OnDemandWorkout 基础功能）
2. **本周**: 完成 Issue #24-26（WorkoutTemplate 全流程）
3. **下周**: 创建 NutritionTip 数据库 + 实现 Issue #27-28
4. **持续**: 补充测试、修复技术债务
