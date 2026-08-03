# PRD: Fitness Coaching App MVP

## Problem Statement

健身小白打开健身 App 时，最难的不是查某个 Exercise，而是不知道今天该练什么、该怎么练、练完后下一步如何调整。现有 Exercise 百科类产品通常把动作库放在第一层，但小白需要的是指导型路径：先建立 UserProfile，再生成 8 周 Plan，每天进入今日 Workout，跟随 Prescription 完成 Exercise，并在完成后提交 ExerciseFeedback。

当前项目已经完成领域建模、技术栈初始化、Exercise 数据导入与详情 API/页面骨架。下一阶段需要把项目从“Exercise 可浏览”推进到“一期 MVP 可用”：UserProfile → Generator → Plan → Workout 打卡闭环。

## Solution

构建一个以 Plan 和 Workout 为核心的指导型健身应用。一期 MVP 聚焦训练闭环：

- User 创建或更新 UserProfile，包括 FitnessLevel、Goal、daysPerWeek、AvailableEquipment。
- 系统基于 UserProfile 生成默认 8 周 Plan。
- Plan 由多个 Workout 组成，每个 Workout 包含线性 WorkoutBlock 和若干 Prescription。
- Generator 优先使用 Template 的骨架；未命中 Template 时降级生成，不出现空白态。
- User 每天查看今日 Workout，按 Prescription 完成 Exercise。
- User 对单次 Exercise 提交 ExerciseFeedback。
- Exercise 详情页作为 Plan/Workout 落地后的二级入口，用于查看 GIF、图片、Target、Equipment、instruction_steps。

## User Stories

1. As a User, I want to create a UserProfile, so that the app can understand my current training situation.
2. As a User, I want to choose my FitnessLevel, so that the app does not prescribe work that is too hard or too easy.
3. As a User, I want to choose my Goal, so that my Plan aligns with fat loss, muscle gain, endurance, or general fitness.
4. As a User, I want to choose daysPerWeek, so that the Plan fits my available schedule.
5. As a User, I want to choose AvailableEquipment, so that Workout only includes Exercise I can actually perform.
6. As a User, I want the app to generate a Plan from my UserProfile, so that I do not need to design my own program.
7. As a User, I want the generated Plan to cover 8 weeks by default, so that I have a clear medium-term training path.
8. As a User, I want only one ACTIVE Plan at a time, so that I know which Plan I am following.
9. As a User, I want future Plans to be SCHEDULED, so that I can prepare a next Plan without confusing it with the current one.
10. As a User, I want a regenerated Plan to supersede the old Plan, so that history is preserved but current guidance is clear.
11. As a User, I want a completed 8 week Plan to renew automatically when enabled, so that I can continue without manual setup.
12. As a User, I want to disable automatic renewal, so that I can decide whether to continue or adjust my Goal.
13. As a User, I want today’s Workout to be easy to find, so that I can start training quickly.
14. As a User, I want each Workout to have a TrainingDayFocus, so that I understand the purpose of the day.
15. As a User, I want Push, Pull, Legs, and FullBody days, so that the Plan feels structured and understandable.
16. As a User, I want Core work to appear as an attached Exercise, so that I do not confuse it with a separate TrainingDayFocus.
17. As a User, I want each Prescription to show sets and reps, so that I know what to do.
18. As a User, I want Prescription to support loadType and rpe, so that effort can be expressed without requiring exact weights.
19. As a User, I want BODYWEIGHT Exercise to be clearly supported, so that I can train without gym equipment.
20. As a User, I want to view Exercise details from a Workout, so that I can confirm form and steps.
21. As a User, I want Exercise details to show GIF and image media, so that I can visually understand the movement.
22. As a User, I want Exercise details to show instruction_steps in my available language, so that I can follow safely.
23. As a User, I want Exercise to show BodyPart, Target, Equipment, and muscle_group, so that I understand what it trains.
24. As a User, I want to search Exercise by name, so that I can quickly find an Exercise.
25. As a User, I want to browse Exercise by BodyPart, so that I can explore the library when needed.
26. As a User, I want Cardio, Lower Arms, and Neck Exercise to remain searchable, so that data is not lost even if they are not primary UI tabs.
27. As a User, I want the Plan to avoid unavailable Equipment, so that I do not get stuck before training.
28. As a User, I want unavailable Exercise to have ExerciseSubstitute options later, so that the Plan remains flexible.
29. As a User, I want to submit TOO_EASY feedback, so that future Prescription can become more suitable.
30. As a User, I want to submit JUST_RIGHT feedback, so that the app knows the Prescription is appropriate.
31. As a User, I want to submit TOO_HARD feedback, so that future Prescription can be adjusted down.
32. As a User, I want to submit HURT feedback by body part, so that unsafe Exercise can be filtered.
33. As a User, I want HURT feedback to affect the current Workout immediately, so that I can continue safely.
34. As a User, I want HURT feedback to affect future Plans for 4 weeks, so that repeated irritation is avoided.
35. As a User, I want missed training to pause my Plan after a threshold, so that the app reflects my real behavior.
36. As a User, I want a PAUSED Plan to become CANCELLED after prolonged inactivity, so that stale guidance does not stay active forever.
37. As a User, I want completed Workouts to remain in history, so that I can see progress over time.
38. As a User, I want frontend pages to show loading and error states, so that API delays or failures are understandable.
39. As a User, I want all API errors to use a consistent JSON shape, so that frontend behavior is predictable.
40. As a developer, I want business logic in Service modules, so that Controllers remain thin and testable.
41. As a developer, I want Generator logic isolated, so that Plan generation can be tested without HTTP or database setup.
42. As a developer, I want Mapper SQL to remain parameterized, so that SQL injection risk is controlled.
43. As a developer, I want JSONB fields to be mapped consistently, so that UserProfile snapshots and instruction_steps remain reliable.
44. As a developer, I want local middleware for PostgreSQL and Redis, so that the environment is reproducible.
45. As a developer, I want GitHub Issues to hold PRDs and implementation slices, so that agents can pick up scoped work.

## Implementation Decisions

- The project remains a Java 21 + Spring Boot 3.5.x backend with Vue 3.5.x frontend.
- PostgreSQL is the source of truth for application data; JSONB is used for structured snapshots and dataset fields.
- Redis is available for future distributed locks where concurrency-sensitive flows require it.
- REST APIs return a unified JSON response: `{code, message, data}`.
- Controllers validate parameters and wrap responses only; business logic lives in Services.
- Java APIs use a global exception handler and custom ErrorCode values.
- Exercise IDs preserve the dataset’s original string IDs.
- Exercise data is imported from `exercises-dataset` and stored without F.1 normalization.
- Exercise media paths are converted to stable GitHub raw URLs for image/GIF display.
- UserProfile is a first-class model and is snapshotted into Plan at generation time.
- Changing UserProfile after generation does not mutate existing ACTIVE or historical Plans.
- Generating a new Plan marks the old current Plan as SUPERSEDED when applicable.
- PlanStatus supports DRAFT, SCHEDULED, ACTIVE, PAUSED, COMPLETED, SUPERSEDED, and CANCELLED.
- The system allows at most one ACTIVE Plan per User.
- The system may allow multiple SCHEDULED Plans ordered by startDate.
- Template defines weekly structure and slot counts; Generator chooses concrete Exercise.
- MVP WorkoutBlock supports only linear sequences.
- TrainingDayFocus is Push, Pull, Legs, or FullBody.
- Core is not a TrainingDayFocus; Core is represented through Exercise with Waist BodyPart.
- FullBody days include one Core Exercise.
- Push and Pull days append one Core Exercise.
- Legs days append two Core Exercises.
- Prescription includes sets, reps, load, loadType, and rpe.
- RPE is preferred; load is fallback.
- ExerciseFeedback is collected per Exercise within a Workout.
- HURT feedback triggers immediate current Workout substitution and future filtering for 4 weeks.
- ExerciseSubstitute is modeled as a relation from one Exercise to another with SubstituteReason.
- MVP can defer full ExerciseSubstitute editorial workflow until the main Plan/Workout loop works.
- NutritionTip, NutritionRule, MacroTarget, NutritionTiming, KnowledgeArticle, and ArticleReference are out of一期 MVP and reserved for later phases.
- Frontend uses Vue Router for page navigation and Element Plus for UI components.
- Frontend Exercise list and detail are the first implemented browsing surfaces.
- Future frontend pages include UserProfile form, Plan view, and today’s Workout.

## Testing Decisions

- Tests should verify external behavior and domain rules, not private implementation details.
- Controller tests use Web MVC slice tests with mocked Services.
- Existing prior art: Exercise Controller tests verify success responses, 404 responses, paging, filtering, search, and validation.
- Service tests should cover business behavior such as UserProfile snapshotting, PlanStatus changes, ACTIVE Plan constraints, FeedbackEffect, and Generator orchestration.
- Generator should be tested as a pure or near-pure module seam: input UserProfile, Template/rules, and Exercise candidates; output Plan, Workout, and Prescription structure.
- Mapper tests are reserved for SQL/JSONB behavior that cannot be safely verified at higher seams.
- Frontend tests should cover API wrappers and critical page behavior rather than internal component details.
- Visual QA is needed for Exercise detail, UserProfile form, Plan view, and Workout check-in flows.
- Concurrency-sensitive flows such as active Plan switching or future inventory-like operations must explicitly use optimistic locking or Redis distributed locks.

## Out of Scope

- NutritionTip and NutritionRule implementation.
- KnowledgeArticle and ArticleReference implementation.
- Food library or food substitution.
- Food conversion notes such as “4 eggs” or “1 chicken breast”.
- User-generated content.
- Full ExerciseSubstitute editor workflow.
- Circuit or superset WorkoutBlock support.
- RequiredSpace modeling.
- Authentication hardening beyond what is required for MVP local development.
- Production deployment automation.
- Payment, subscription, social sharing, coaching marketplace, or wearable integrations.

## Further Notes

- The project should continue as vertical slices rather than broad horizontal layers.
- Slice 1 is functionally close to complete but still needs frontend visual QA before being marked done.
- Slice 2 should implement UserProfile end to end.
- Slice 3 should implement Generator and Plan viewing.
- Slice 4 should implement today’s Workout and ExerciseFeedback.
- After this PRD is published, `/to-issues` should split it into independently grabbable GitHub Issues with the `ready-for-agent` label.
