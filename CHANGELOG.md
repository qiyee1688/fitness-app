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
