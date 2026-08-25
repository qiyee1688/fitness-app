# 健身指导产品对比与功能取舍（2026-08-25）

## 研究问题与方法

本报告回答：在本项目已确定的 **Plan → 今日 Workout → 打卡 → ExerciseFeedback** 主链路，以及按需训练、模板、训练附属 NutritionTip、KnowledgeArticle、FoodItem 方向上，还应借鉴、延后或明确不做哪些能力。

资料仅使用产品自己发布的帮助中心、官网、投资者关系页和 App Store 开发者条目；功能会随产品迭代，以下是检索日可核验的事实。每项“建议”都是基于这些事实与本仓库的领域决策作出的推断，而不是来源方的主张。

## 项目基线

`CONTEXT.md` 已锁定本产品为面向初学者的指导型应用：饮食只附属于 Workout，内容由官方编辑发布而不开放 UGC；核心数据与二期能力已经涵盖 UserProfile、8 周 Plan、按需 Workout、WorkoutTemplate、Exercise 级反馈及 NutritionTip。当前开放 Issue 的下一条明确交付是 FoodItem 份数换算与文章入口（[#38](https://github.com/qiyee1688/fitness-app/issues/38)、[#39](https://github.com/qiyee1688/fitness-app/issues/39)）；不应以竞品调研打断这条已拆分的交付链。

## 可核验的竞品事实

| 产品 | 官方资料可核验的能力 | 对本项目的启示（推断） |
| --- | --- | --- |
| Fitbod | 训练建议综合目标、经验、可用器械、训练时长、历史与肌肉恢复；可按记录的组、次数、重量与 RiR 调整后续建议，并允许用户控制动作的“更多／更少／不再推荐”。[训练生成机制](https://help.fitbod.me/hc/en-us/sections/360001078993-Understanding-Fitbod-How-It-Works)；[恢复追踪](https://help.fitbod.me/hc/en-us/articles/360006269014-Muscle-Recovery)；[动作偏好](https://help.fitbod.me/hc/en-us/articles/9093233634711-Recommend-More-Less-or-Exclude-Exercises) | 已有 ExerciseFeedback、HURT 过滤、器械过滤和 RPE；下一步的缺口不是另一个生成器，而是将“完成数据 + 主观难度”变成可解释的下一次微调。 |
| Nike Training Club | NTC 官方页面提供教练带领的 200+ 免费训练，覆盖力量、耐力、瑜伽和灵活性，单节 5–50 分钟；也有多周计划、进度与里程碑。[NTC 官方页](https://www.nike.com/gb/ntc-app) | 对初学者，“跟练视频 + 清晰的课程包装”能提升可执行性；但本项目的差异应是个性化处方和动作提示，不是建设大规模版权课程平台。 |
| Freeletics | Coach 在入门问卷后生成 Journey，并按每次训练的表现和反馈持续适配；用户可选择周训练日和器械，并在当前训练中按时间、器械、场地、安静需求、训练部位或难度生成替代 Session。[Coach 入门流程](https://help.freeletics.com/hc/en-us/articles/115004675229-Get-started-with-Freeletics-Training)；[训练适配选项](https://help.freeletics.com/hc/en-us/articles/360003933780-Adapt-your-Bodyweight-training-session)；[Journey 适配](https://help.freeletics.com/hc/en-us/articles/360001805519-Choose-your-Freeletics-Training-Journey) | “当日约束”是按需训练比长期 Plan 更重要的输入。项目已有本次临时器械、替换动作和按需训练；可补足的是时间预算与动作／部位回避，而非照搬整套 Coach 产品。 |
| Hevy | 可创建复用 Routine，配置动作、组、负重／次数区间、时长和自动休息；支持重新排序、替换、删除、热身组和超级组。[训练记录与 Routine](https://www.hevyapp.com/features/track-workouts/)；其动作库提供器械／肌群筛选、动作演示、历史表现与执行说明。[动作库](https://help.hevyapp.com/hc/en-us/articles/35688251991575-Hevy-Exercise-Library-400-Exercises-and-Custom-Exercises) | 已规划 Template 的轻量编辑和 Exercise 详情，可优先补足“上次表现”与休息计时；复杂超级组/自由编排应继续延后，避免把指导型产品变成记录器。 |
| Strong | 定位为训练计划与记录；官方列出的功能包括个人纪录、1RM、RPE、进阶图表、训练排程、肌肉热图、超级组、自定义动作、数据导出和跨设备访问。[Strong 官方产品页](https://www.strong.app/) | 其功能集合说明高级日志工具有需求，但它服务的是自主编排训练者；和本项目“小白先知道今天练什么”的定位不匹配，宜作为远期可选能力而非路线图中心。 |
| Keep（中国相关样本） | Keep 在中国区官方 App Store 条目描述 AI 训练安排、健身记录、跑步陪伴、吃练睡数据分析、课程/直播、赛事活动、社区与多类运动健康数据整合。[Keep — AI 运动教练](https://apps.apple.com/cn/app/keep-ai-%E8%BF%90%E5%8A%A8%E6%95%99%E7%BB%83/id952694580)；其官方投资者关系页还描述 AI 辅助个性化课程及软硬件、内容和消费品的一站式生态。[公司介绍](https://ir.keep.com/en/about_profile.php) | 中国用户熟悉“课程＋记录＋社区＋设备”的大平台体验；本项目可借鉴本地化语言和低门槛内容入口，但不应在早期复制社区、直播、电商或硬件生态。 |

## 建议纳入近期路线图

1. **完成三期已在途的 FoodItem 换算与文章入口。** 它与“训练后不知道怎么吃”的初学者问题一致，且仍保持 Diet 为 Workout/科普的附属。先完成 #38、#39；不扩张成饮食日志或配餐器。
2. **在现有 `ExerciseFeedback` 上加入可解释的“下次建议微调”。** 先只使用已采集的 `TOO_EASY`、`JUST_RIGHT`、`TOO_HARD`、`HURT_*` 和实际完成组次：例如连续 `TOO_EASY` 才提示提高一个确定的处方维度，连续 `TOO_HARD` 则降级或替换。把调整原因显示出来。这个小闭环吸收 Fitbod/Freeletics 的适应价值，却不需要宣称 AI 或引入黑箱模型。
3. **为 OnDemandWorkout 增加一个可选“可用时长”约束。** 当日的 15/30/45 分钟选择比扩展部位枚举更能解决真实变化；生成器据此缩放动作数量或组数，并明确告诉用户削减了什么。应保留当前“候选不足即业务错误、不重复动作”的约束，不能悄悄产出残缺训练。
4. **补齐执行层的基础可用性：组间休息计时、上次表现、完成后小结。** 这些是 Hevy/Strong 的低复杂度高频价值，不改变领域边界；“上次表现”必须只展示用户自己的历史并复用统一 Workout/Set 模型。
5. **把动作回避从受伤反馈扩展为用户主动偏好，但保持小范围。** 可先提供“本计划中少出现/不出现”与“本次换一个动作”，并与 `ExerciseSubstitute` 和器械校验共用一条规则。不要让用户直接从全库任意拼 Workout——这会绕开既定的模板最小动作数与教练引导。

## 应明确延后（验证留存后再决定）

| 能力 | 延后理由与重启门槛 |
| --- | --- |
| 肌群恢复热图、外部健康数据导入 | 需定义动作到肌群的工作量算法、处理多设备和隐私授权；当前已有 HURT 与未打卡软降级。待连续打卡和反馈数据足以验证“下一次推荐”质量后，再以只读恢复提示做小实验。 |
| 真实自适应周期化、自动改重量/次数 | 需要高质量历史 Set 数据、算法安全边界和可解释回退。先交付规则化、用户确认后的处方微调；不以“AI”承诺训练效果。 |
| 视频课程、直播、明星教练内容 | NTC/Keep 的内容优势依赖长期版权、制作和运营；项目已有开源动作多媒体、CoachCue 和官方文章，后者更符合成本与定位。只有核心训练闭环留存稳定后才评估。 |
| 模板的 Circuit / Superset / 自由添加全库动作 | `CONTEXT.md` 已明确二期不做，Hevy/Strong 的成熟日志器功能不适合抢在初学者闭环之前。重启条件：模板使用率高，且访谈表明线性 Workout 无法覆盖主要需求。 |
| 社区、动态、评论、挑战赛、排行榜 | Keep/Hevy 的社交功能意味着审核、滥用处理、隐私与运营负担；本项目已决定 KnowledgeArticle 由官方编辑发布。只有另立社区领域、明确内容治理与数据策略后才可开题。 |
| 穿戴设备/HealthKit/Health Connect 同步、跑步 GPS | 跨平台健康数据需要授权、冲突处理、后台任务和隐私合规，且会将范围从力量训练指导扩展为泛运动记录。未来可先评估只写出已完成 Workout 的单向导出。 |

## 应继续排除或避免

1. **独立 Diet、自动配餐、卡路里/食物日记和用户自定义 FoodItem。** 这直接违背“Diet 不作为独立支柱”的已决边界，也会把静态审核 FoodItem 目录演变成高维护的营养数据库。
2. **UGC 社区、评论、达人内容市场、直播和电商/硬件销售。** 它们是 Keep 的生态策略，而非本项目核心价值；会引入审核、内容供给和支付履约等全新上下文。
3. **把伤痛处理包装为医疗建议或诊断。** Fitbod 也明确其伤病建议不能替代专业医疗指导；本项目只应提供保守过滤、替换和就医提示，而不判断伤情或康复方案。[Fitbod 伤病与限制说明](https://help.fitbod.me/hc/en-us/articles/37629269518103-Injuries-and-Limitations)
4. **不透明地自动重写用户已开始或已完成的 Workout。** 竞品会重生成下一次建议，但本项目已有 Workout/Tip 快照和替换链决策；历史应保持可追溯，任何改动应创建新版本或要求确认。
5. **以 1RM、PR、肌肉热图和复杂图表作为新手首页主信息。** 这些可作为后续用户主动查看的记录页信息；首页应继续回答“今天练什么、怎么练、完成得如何”。

## 推荐的顺序与衡量

1. **现在：** #38 → #39，完成 FoodItem 科普闭环。
2. **随后：** 用一张小规格设计卡确定“训练时长”和“处方微调”的输入、边界、文案与安全规则；在实现前写测试用例，避免生成器分叉。
3. **再后：** 休息计时、上次表现、完成小结；只记录能够帮助下一次 Workout 的指标。
4. **验证指标（产品假设，非外部事实）：** 首周完成率、第二周完成率、`TOO_HARD` 后仍继续训练的比例、OnDemandWorkout 生成后开始率、Template 保存后复用率、FoodItem/文章入口到 Workout 的回流率。若前三项没有改善，不进入恢复算法、社交或内容重资产。

## 来源目录

- Fitbod: [训练生成与适应](https://help.fitbod.me/hc/en-us/sections/360001078993-Understanding-Fitbod-How-It-Works)、[肌肉恢复](https://help.fitbod.me/hc/en-us/articles/360006269014-Muscle-Recovery)、[动作偏好](https://help.fitbod.me/hc/en-us/articles/9093233634711-Recommend-More-Less-or-Exclude-Exercises)。
- Nike Training Club: [官方应用介绍](https://www.nike.com/gb/ntc-app)。
- Freeletics: [Coach 入门](https://help.freeletics.com/hc/en-us/articles/115004675229-Get-started-with-Freeletics-Training)、[训练适配](https://help.freeletics.com/hc/en-us/articles/360003933780-Adapt-your-Bodyweight-training-session)、[Journey](https://help.freeletics.com/hc/en-us/articles/360001805519-Choose-your-Freeletics-Training-Journey)。
- Hevy: [训练记录与 Routine](https://www.hevyapp.com/features/track-workouts/)、[动作库](https://help.hevyapp.com/hc/en-us/articles/35688251991575-Hevy-Exercise-Library-400-Exercises-and-Custom-Exercises)。
- Strong: [官方产品页](https://www.strong.app/)。
- Keep: [中国区官方 App Store 条目](https://apps.apple.com/cn/app/keep-ai-%E8%BF%90%E5%8A%A8%E6%95%99%E7%BB%83/id952694580)、[官方投资者关系公司介绍](https://ir.keep.com/en/about_profile.php)。
