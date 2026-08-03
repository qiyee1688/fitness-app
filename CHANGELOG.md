# Fitness App 修改记录

记录每次会话的文件创建/修改，用于快速恢复上下文。

---

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
