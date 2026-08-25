# PRD：四期 4A — 基于反馈的可解释处方微调

## 目标

在完成三期 FoodItem 换算与文章入口（#38、#39）后，验证“反馈让下一次训练更合适”这一假设。系统不宣称 AI 自动训练，而是基于现有 ExerciseFeedback 给出小幅、可解释且必须经用户确认的下一次处方建议。

## 范围

- 只读取 Active Plan 中同一 Exercise 的连续 `TOO_EASY` 或 `TOO_HARD` Feedback。
- 连续两次相同反馈后，创建一条 PrescriptionAdjustment 审计记录和待确认建议。
- `TOO_EASY` 按顺序建议提高 RPE、次数或一个小档负重；`TOO_HARD` 反向调整。一次只改一个维度，且不得超过当前 FitnessLevel 安全范围。
- 连续 `TOO_HARD` 可建议符合现有器械和 Substitute 规则的 ExerciseSubstitute。
- 用户在 Workout 完成小结或下次训练开始前查看原处方、建议处方和原因，并确认或拒绝。
- 确认后，在乐观锁保护下仅更新同一 Exercise 的下一次未开始 Prescription，并保留完整审计记录。
- 确认、拒绝、没有下一次目标、目标 Workout 被替换/取消或 Plan 再生成，都会消耗当前反馈窗口；目标无效时记录 `EXPIRED`，不得跨 Plan 迁移。

## 非目标

- 不新增实际组数、次数、负重日志，也不使用估算 1RM。
- 不自动生效或批量改写当前 Plan、历史 Workout、OnDemandWorkout、WorkoutTemplate 或自动续期 Plan。
- 不替代 `HURT_*` 的即时替换与未来四周过滤。
- 不实现完整恢复算法、肌群热图、自动周期化、自由编排或医疗化伤痛建议。

## 领域与状态

`PrescriptionAdjustment` 保存：稳定 ID、source workout/exercise/feedback、target workout/prescription（可空）、原处方快照、建议处方快照或 Substitute、原因、状态、创建/处理时间。

状态：

- `PENDING`：存在可调整的下一次目标，等待用户操作。
- `ACCEPTED`：用户确认，目标 Prescription 在乐观锁保护下更新。
- `DECLINED`：用户拒绝，处方不变。
- `EXPIRED`：没有目标，或目标因替换、取消、Plan 重生成而无效。

## 验收条件

1. 同一 Active Plan 的同一 Exercise 连续两次 `TOO_EASY` 或 `TOO_HARD` 才产生建议；`JUST_RIGHT` 或不同类型反馈不构成连续触发。
2. 建议清楚显示触发反馈、原处方、单一改动后的建议处方，以及“为什么调整”的文案。
3. 确认只影响下一次未开始目标；已开始/已完成 Workout、NutritionTip 快照和其它未来 Workout 不变。
4. 确认使用 Plan 或目标记录的乐观锁；冲突返回统一业务错误且不产生重复应用。
5. 拒绝、确认、无目标和目标失效均保留审计记录并消耗反馈窗口；新建议必须依赖新的连续反馈。
6. `TOO_HARD` 的动作替代仅能使用当前可用的 ExerciseSubstitute；无合规替代时保留处方降级建议或给出明确原因。
7. 完成小结与下次训练入口均支持加载、空态、失败和双语展示；不干扰 Workout 完成。
8. 后端 Migration、Service、Controller、并发与审计测试，以及前端关键流程测试和生产构建通过。

## 成功指标

主指标：已接受调整后，该 Exercise 的下一次反馈为 `JUST_RIGHT` 的比例。

护栏指标：建议确认率、接受调整用户的次周训练完成率、`TOO_HARD` 后继续训练比例。指标仅作为产品假设验证，不在本阶段引入外部分析平台。

## 交付切片与阻塞关系

1. 4A-1：Adjustment 持久化、反馈历史查询、候选生成与失效状态。
2. 4A-2：确认/拒绝 API、目标处方乐观锁应用与审计测试；阻塞于 4A-1。
3. 4A-3：完成小结与下次训练确认界面；阻塞于 4A-2。
