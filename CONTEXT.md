# Fitness Coaching App

面向健身小白的指导型应用。基于 https://github.com/hasaneyldrm/exercises-dataset 提供的 1,324 条 Exercise 数据（多媒体、10 语言步骤）扩展。核心是"今天该练什么、该怎么练"，Exercise 百科是计划落地后的二级入口。Diet 不作为独立支柱，只作为 Workout 附属。KnowledgeArticle 由官方编辑撰写，UGC 口吻，可引用 Exercise。

> 实现细节一律不入本文件。本文件只锁词汇和已决领域决策。

## Language

### 内容（来自 / 同步自数据集）

**Exercise**:
一个独立的健身动作（深蹲、卧推）。从 `exercises-dataset` 导入，字段含 `id / name / category / body_part / equipment / target / muscle_group / secondary_muscles / instruction_steps[lang] / gif_url / image`。
_Avoid_: 动作、训练动作、动作模板

**BodyPart**:
Exercise 的粗粒度人体部位分类，用于 UI 部位 Tab。数据集枚举：`Upper Arms / Upper Legs / Back / Waist / Chest / Shoulders / Lower Legs / Lower Arms / Cardio / Neck`。
_Avoid_: 部位、分类

**Target**:
Exercise 的目标肌群（细粒度），用于生成器筛选。例：`abs / quads / pecs / delts`。
_Avoid_: 主肌群（与 `muscle_group` 区分，`muscle_group` 是协同肌群，`target` 是被练肌群）、目标肌肉

**Equipment**:
Exercise 所需的器械。`body weight` 视为真正的"无器械"（MVP 不细分场地）。
_Avoid_: 器械、工具

**ExerciseSubstitute**:
两个 Exercise 之间的"可替代"关联实体，含 `from: ExerciseId` / `to: ExerciseId` / `reason: SubstituteReason`。用户在友好模式下，被过滤掉的 Exercise 由此关系自动找到替代品。建法：自动按 `body_part + target + equipment 相似` 聚类出候选，编辑一键 confirm。**强依赖 F.1 归一化完成后才能执行**。
_Avoid_: 相似动作、备选

**SubstituteReason**:
替代动机枚举：`EQUIPMENT_SWAP`（器械替换，主用途）/ `DIFFICULTY_DOWNGRADE`（难度降级）/ `INJURY_FRIENDLY`（伤情友好）/ `OTHER`（其他）。二期 MVP 只暴露 EQUIPMENT_SWAP，其余 v2 补。
_Avoid_: 替代原因

### 计划 / 编排

**Plan**:
一个用户在某段时间内（默认 8 周）的完整训练规划，包含多个 Workout 与一份用户档案快照。同时间最多 1 个 `ACTIVE` Plan，可排队多个 `SCHEDULED` Plan（按 startDate 升序排队，到期自动接力）。Plan 之间**独立 startDate，不自动衔接**——用户手动或引导触发下条 Plan。续期时建**子 Plan**（`parentPlanId: PlanId?`）保留父子关系，详见决策 28。
_Avoid_: 计划表、课表

**PlanStatus**:
Plan 的生命周期状态。枚举：`DRAFT`（编辑中未激活）/ `SCHEDULED`（已配 startDate > now，等待激活）/ `ACTIVE`（生效中，单条）/ `PAUSED`（用户连续未打卡时软降级，仍可恢复）/ `COMPLETED`（到期或用户手动结束）/ `SUPERSEDED`（被新 Plan 替代）/ `CANCELLED`（用户主动作废或软降级超时）。迁移规则由 Generator 与用户操作共同驱动，详见决策 22、27。
_Avoid_: 状态、阶段

**Template**:
一份由编辑手工编写的"招牌骨架计划"，只定每周分日与每日槽位数，不定具体动作。用于覆盖 80% 主流用户。未命中模板时降级到生成器。
_Avoid_: 模板、样板

**Generator**:
根据用户档案 + 模板匹配结果，按规则拼出 Plan 的组件。在无模板命中时直接产出完整 Plan。
_Avoid_: 规则引擎、生成器

**Workout**:
一次具体训练日，由若干 `WorkoutBlock` 组成，归属一个 Plan 中的某一天。`Focus` 字段标注本次训练重点。
_Avoid_: 训练、训练日

**WorkoutBlock**:
Workout 内的一组连续动作（线性序列 / 循环 / 复合组）。MVP 阶段只支持线性序列。
_Avoid_: 训练块、组（避免与 `Set` 混淆）

**TrainingDayFocus**:
Workout 的训练重点枚举：`Push / Pull / Legs / FullBody`。归 Plan 上下文，不挂在 Exercise 上。Core 不是 Focus（已从枚举删除），只是动作类型（通过 `body_part=Waist` 识别），作为其他 Focus 的附属收尾动作。
_Avoid_: 训练类型、训练模式

**Prescription**:
一次 Exercise 该怎么做的处方。含 `sets / reps / load / loadType / rpe`。`loadType ∈ {ABSOLUTE_WEIGHT, PERCENT_1RM, BODYWEIGHT, RPE_ONLY, DURATION}`。`rpe` 优先，`load` 作为 fallback。
_Avoid_: 处方、组次

**Set**:
Prescription 中的单个"组"，用户实际完成的次数与负荷可与处方不同。打卡时记录。
_Avoid_: 组次、一组

### 用户 / 反馈

**User**:
App 的注册用户。拥有 `UserProfile`。
_Avoid_: 学员、用户账号

**UserProfile**:
用户的训练档案快照：`fitnessLevel / goal / daysPerWeek / availableEquipment / createdAt`。**生成 Plan 时快照**，之后修改档案不影响已生成的 Plan；新档案生成新 Plan，旧 Plan 标 `SUPERSEDED`。
_Avoid_: 用户资料、档案

**FitnessLevel**:
用户训练水平枚举：`BEGINNER / INTERMEDIATE / ADVANCED`。"小白"是 `BEGINNER` 的口语化表达，不是领域词。
_Avoid_: 小白、菜鸟、级别

**Goal**:
用户目标枚举：`FAT_LOSS / MUSCLE_GAIN / ENDURANCE / GENERAL_FITNESS`。
_Avoid_: 目标、需求

**AvailableEquipment**:
用户当前可用的器械集合。子集匹配规则：用户的器械集合 ⊇ Exercise 的 `equipment` 即匹配。
_Avoid_: 装备、器械清单

**ExerciseFeedback**:
用户对单次 Exercise 完成的反馈。粒度 = Exercise 级。枚举：`TOO_EASY / JUST_RIGHT / TOO_HARD / HURT_<body_part>`。
_Avoid_: 评价、感受

**FeedbackEffect**:
`HURT` 类反馈的双层生效：(1) 当下 Workout 立刻替换该 Exercise；(2) 未来 4 周的所有 Plan 过滤同肌群/同动作。
_Avoid_: 反馈影响、伤痛处理

### 内容扩展（数据集外）

**KnowledgeArticle**:
官方编辑撰写的健身 / 饮食科普文章，UGC 口吻。可引用多个 Exercise（多对多）。
_Avoid_: 帖子、推文、新闻

**Editor**:
文章的作者身份。团队人员，**不是 User**。
_Avoid_: 教练、运营、用户作者

**ArticleReference**:
KnowledgeArticle 与 Exercise 之间的多对多引用（"文中嵌入了哪个动作的 GIF"）。
_Avoid_: 标签、相关动作

**NutritionTip**:
附属在 Workout 上的饮食小贴士。一条 Workout 可挂多条 Tip（最多 3 条：`PRE_WORKOUT` / `POST_WORKOUT` / `DAILY`）。结构是混合：必填 `macroTargets: MacroTarget` + `timing: NutritionTiming`，可选 `note: string`（编辑整段写死、UGC 风格、**不含占位符**）。Diet 不作为独立上下文，所有饮食内容以此形态出现。
_Avoid_: 饮食计划、食谱、一日三餐

**MacroTarget**:
一顿 / 一日 / 练前 / 练后 的宏量素目标。`protein / carbs / fat / kcal` 各自有 `value: number` + `unit: NutritionUnit` + `basis: ABSOLUTE | PER_KG_BODYWEIGHT`。
_Avoid_: 营养目标、宏量素参数

**NutritionUnit**:
宏量素数值的单位。`GRAMS`（克）/`KILOCALORIES`（千卡）。
_Avoid_: 单位

**NutritionTiming**:
Tip 的生效时机。枚举：`PRE_WORKOUT`（练前）/ `POST_WORKOUT`（练后）/ `DAILY`（全天总览）。PRE 与 POST 单条 Workout 最多各 1 条；DAILY 每 Workout 最多 1 条；单 Workout 最多 3 条 Tip。
_Avoid_: 时机、阶段

**NutritionRule**:
编辑手工编写的"营养规则"，定义"什么 Goal × Focus × Timing 下用什么公式"计算 MacroTarget。Rule 含 `condition: {goal, focus, timing}`（**不包含 level**，level 维由 Q20 = β 砍掉，老手/小白差异化只走 MacroTarget 数值）+ `formula: {protein: {value, basis}, carbs: {...}, fat: {...}, kcal: {...}}`（沿用 `MacroTarget` 字段，basis=PER_KG_BODYWEIGHT 时 value 是克每公斤系数）。`note` 不属于 Rule —— note 整段由编辑写死在 `NutritionTip` 上，不含占位符。二期 MVP 只做增肌/减脂 × 3 Timing = 6 条核心 Rule（其余 v2 补），详见决策 25。
_Avoid_: 营养规则、公式

**NutritionNoteStyle**:
`NutritionTip.note` 的写作约束。整段编辑撰写、UGC 口吻、**不允许占位符**、不含食物换算（"4 个蛋"这类换算归三期 KnowledgeArticle）。note 解释"为什么这么吃"，个性化由 `MacroTarget` 数值承载，note 本身不随用户档案变化。
_Avoid_: 模板、口吻

## 已决领域决策（决策历史）

1. **MVP 是指导型**（用户档案 → 今日 Workout → 打卡），Exercise 百科是二级入口。
2. **Diet 附属**于 Workout，无独立饮食上下文。
3. **KnowledgeArticle 由官方编辑撰写，UGC 口吻，可引用 Exercise**。不开放用户投稿。
4. **模板只定骨架**（每周分日 + 每日槽位数），具体动作由生成器按用户器械 / 禁忌实时挑。
5. **未命中模板时降级到生成器**。不允许"暂无合适计划"的空白态。
6. **难度建模选 B 方案**：Exercise 是动作本身，难度在 Prescription 里。数据集 1,324 条需做一次性人工归一（将同动作不同 equipment 合并为单条 Exercise，equipment 变可选）。
7. **负荷表达**：优先 RPE，fallback 重量 × 组数 × 次数。
8. **反馈粒度 = Exercise 级**，每条 Exercise 可独立标反馈。
9. **HURT 类反馈双层生效**：当下换动作 + 未来 4 周过滤。
10. **允许重生成 Plan**，旧 Plan 标 `SUPERSEDED` 保留历史。
11. **友好模式匹配**：被器械过滤掉的 Exercise 通过 `ExerciseSubstitute` 关系自动找替代。
12. **MVP 不补 `RequiredSpace`**，先 ship 后优化。
13. **UI 部位 Tab = 5 个**：练胸 (Chest) / 练背 (Back) / 练肩 (Shoulders) / 练腿 (Upper Legs + Lower Legs) / 练核心 (Waist)。
14. **`body_part` 给 UI 用，`target` 给生成器用**。二者分工明确。
15. **TrainingDayFocus 归 Plan 上下文**，枚举 `Push / Pull / Legs / FullBody / Core`，与 `body_part` 的映射人工配置。
16. **WorkoutBlock MVP 只支持线性序列**。循环 / 复合组留待 v2。
17. **Cardio / Lower Arms / Neck 这 31 条不暴露在部位 Tab**（不删除，保留在"全部动作"页可搜索）。
18. **`NutritionTip` 结构 = 混合**（结构化 MacroTarget + NutritionTiming + 可选口语 note）。
19. **`NutritionTip` 与 Workout 关系 = 1:N**，单 Workout 最多 3 条（PRE_WORKOUT / POST_WORKOUT / DAILY 各 1）。
20. **`MacroTarget.basis` 双轨**：优先 `PER_KG_BODYWEIGHT`（按用户体重算），fallback `ABSOLUTE`（编辑写死克数 / 千卡）。
21. **`NutritionTip` 来源 = 半自动**：编辑写 `NutritionRule`（condition + 公式），生成器按用户档案 + Rule 实时算 `MacroTarget` 数值（PER_KG_BODYWEIGHT 时按用户体重算克数）。`note` **不属于 Rule**，整段由编辑写死在 `NutritionTip` 上，无占位符，详见决策 22-23。
22. **`Plan.status` 状态机 = `DRAFT | SCHEDULED | ACTIVE | PAUSED | COMPLETED | SUPERSEDED | CANCELLED`**（7 态）。同时间最多 1 个 ACTIVE，可排队多个 SCHEDULED。Plan 之间独立 startDate，不自动衔接。回炉决策 10（旧版本只列了 ACTIVE/SUPERSEDED/COMPLETED 三态），再回炉扩展 PAUSED（决策 27）。
23. **`NutritionTip.note` 整段编辑写死，不允许占位符**。note 解释"为什么这么吃"；个性化由 `MacroTarget` 数值承载，note 不随用户档案变化。回炉决策 21（旧版本把 note 当模板填）。
24. **食物换算（"4 个蛋"、"1 块鸡胸"）归三期 KnowledgeArticle + 食物库**，二期 NutritionTip 不做食物换算。
25. **`NutritionRule.condition` 砍 level 维**，剩 `goal × focus × timing = 60` 条 Rule 理论上限。二期 MVP 只做增肌/减脂 × 3 Timing = **6 条核心 Rule**（其余 v2 补）。老手/小白差异化只走 MacroTarget 数值（决策 23）+ note 风格层面。
26. **8 周 Plan 到期默认 = 引导 + 一键续期**：弹窗"是否再来一轮 / 调整目标"，提供"一键续期"快捷按钮，进阶用户点"调整目标"深入定制。
27. **中途放弃 Plan 走 PAUSED → CANCELLED 软降级**：连续 2 周未打卡 → 标 `PAUSED`；再 2 周未恢复 → 自动 `CANCELLED`。触发决策 22 扩展 PAUSED（6 态 → 7 态）。
28. **续期 Plan 用 `parentPlanId` 父子关系**——续期建子 Plan，父 Plan 标 `COMPLETED`，可按 parentPlanId 聚合查"用户所有增肌 Plan"。
29. **`ExerciseSubstitute` 建法 = 自动建议 + 编辑一键 confirm**：脚本按 `body_part + target + equipment 相似` 聚类出候选，编辑 confirm。**强依赖 F.1 归一化**——归一化没做完，聚类跑不动。
30. **`ExerciseSubstitute` 是关联表实体**，含 `from / to / reason: SubstituteReason`。`SubstituteReason` 枚举：`EQUIPMENT_SWAP`（二期暴露）/ `DIFFICULTY_DOWNGRADE` / `INJURY_FRIENDLY` / `OTHER`（后三者 v2 补）。
31. **F.1 归一化决策 = 不归一**：数据集 1,324 条保持原样，不合并同动作不同器械。饮食小贴士直接引用 Exercise.id；计划生成器按器械字段匹配友好模式。
32. **F.1 落地影响**：决策 6 β 方案（难度在 Prescription 里）已执行，但归一化步骤不做；决策 29 `ExerciseSubstitute` 建法不变（聚类仍按 body_part + target + equipment 相似）。
33. **F.2 替换配置决策 = 混合模式**：默认自动换 1 个替代动作（按决策 29-30 聚类建立的 ExerciseSubstitute 关系），用户可从动作详情页手动切换其他替代品（UI 展示该动作的所有 ExerciseSubstitute.to 列表）。
34. **F.5 续期决策 = 系统自动续期**：8 周 Plan 到期后自动建子 Plan（parentPlanId 指向父 Plan，决策 28），用户可在设置里关闭自动续期（关闭后走决策 26 引导 + 一键续期流程）。
35. **F.7 Rule 工作量决策 = 一期只写前 50 个热门动作**：Rule 总量 60 条（决策 25），一期只写前 50 个热门 Exercise 的 Rule（工作量 ~50 条），二期补全剩余 1,274 条动作的 Rule。
36. **隐患 α Note 风格决策 = 轻松口语风格**：`NutritionTip.note` 使用轻松口语风格（"深蹲时膝盖别往里扣，跟着脚尖方向走就对了"），目标用户是小白，友好优先。
37. **F.4 FullBody 编排规则 = 轮换式全身**：3 个 FullBody 日**合起来**覆盖全身，单次可偏重（随机挑 2-3 个大肌群），不做硬性全周覆盖检查，依赖"8 周下来大概率练到所有肌群"。**FullBody 日强制包含 1 个 Core 动作**。
38. **F.4 FullBody 与分化训练混合编排**：FullBody 可以和 Push/Pull/Legs 混用（例：4 天 → FullBody / Push / Pull / Legs），FullBody 作为平衡日。
39. **F.4 Core 附属规则 = 按 Focus 类型选择性加**：Push/Pull 日最后加 1 个 Core；Legs 日最后加 2 个 Core；FullBody 日已强制包含 1 个 Core（决策 37），不额外加。
40. **F.4 TrainingDayFocus 枚举修正**：从 5 个改为 4 个（`Push / Pull / Legs / FullBody`），删掉 `Core`。Core 不是 Focus，只是动作类型（`body_part=Waist`），作为其他 Focus 的附属收尾动作。

## 分期

- **一期**：Plan 生成器 + Workout 打卡 + Exercise 详情。无饮食、无科普。
- **二期**：加 `NutritionTip` + `NutritionRule` + `MacroTarget` + `NutritionTiming` 附属到 Workout（每 Workout 最多 3 条 Tip：PRE_WORKOUT / POST_WORKOUT / DAILY）。
- **三期**：加 `KnowledgeArticle` 与 `ArticleReference`。

## 开放分叉（待后续决策）

- **F.1** ~~数据集归一化负责人与时间表（β 方案需 2-3 编辑约一周）~~ → **已关闭**（决策 31-32 锁：饮食小贴士直接引用 Exercise.id；计划生成器按器械字段匹配友好模式）。
- **F.2** ~~`ExerciseSubstitute` 由编辑人工配置还是按 `instruction_steps` 相似度自动出建议再人工审核~~ → **已关闭**（决策 29-30 锁：自动按 `body_part + target + equipment 相似` 聚类 + 编辑一键 confirm；关联表 + reason 字段；决策 33 锁：混合模式，默认自动换 1 个替代动作，用户可从详情页手动切换）。
- **F.3** ~~一期是否包含 `NutritionTip`~~ → **已关闭**（明确归二期）。
- **F.4** ~~`FullBody` 日的具体编排规则（与 `Push/Pull/Legs` 是否有"必须包含的肌群清单"差异）~~ → **已关闭**（决策 37-40 锁：轮换式全身，随机挑 2-3 大肌群 + 强制 1 个 Core，不做全周覆盖检查；与分化训练混合编排；Core 按 Focus 类型选择性加；TrainingDayFocus 枚举改为 4 个）。
- **F.5** ~~8 周计划到期是自动续期还是引导重生成~~ → **已关闭**（决策 26 锁：引导 + 一键续期；决策 27 锁 PAUSED 软降级；决策 28 锁 parentPlanId 父子关系；决策 34 锁：系统自动续期 + 用户可在设置里关闭）。
- **F.6** ~~多 Plan 并行 / 未来生效~~ → **已关闭**（决策 22 锁：状态机扩到 7 态，最多 1 ACTIVE，可排队 SCHEDULED，独立 startDate）。
- **F.7** ~~`NutritionRule` 模板工作量（180 条笛卡尔积）~~ → **已关闭**（决策 25 锁：condition 砍 level 维 = 60 条上限，二期 MVP 只做 6 条核心 Rule + 6 段 note；决策 35 锁：Rule 一期只写前 50 个热门动作，工作量 ~50 条，二期补全）。
- **F.8**（隐患 α）~~note 模板填充 vs 编辑整段写死~~ → **已关闭**（决策 23 锁：note 整段写死无占位符；个性化走 MacroTarget 数值；食物换算归三期，决策 24；决策 36 锁：Note 使用轻松口语风格，目标用户是小白）。
