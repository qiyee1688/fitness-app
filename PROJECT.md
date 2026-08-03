# Fitness App 项目框架

## 项目概述

面向健身小白的指导型应用。核心功能：根据用户档案生成 8 周训练计划 + 每日 Workout 打卡 + Exercise 动作详情。

基于 [exercises-dataset](https://github.com/hasaneyldrm/exercises-dataset) 提供的 1,324 条 Exercise 数据。

---

## 技术栈

### 后端
- **框架**：Spring Boot 3.5.16
- **ORM**：MyBatis Spring Boot Starter 3.0.5（XML Mapper）
- **数据库**：PostgreSQL 15+
- **构建工具**：Maven 3.8+
- **Java 版本**：Java 21 LTS（本机目标：21.0.7）
- **并发模型**：Spring Virtual Threads 开启

### 前端
- **框架**：Vue 3.5.40 + Vite 8.2.0
- **UI 组件库**：Element Plus 2.14.3
- **状态管理**：Pinia 4.0.2
- **路由**：Vue Router 4.6.4

### 开发环境
- **容器**：Docker Compose（PostgreSQL）
- **IDE**：IntelliJ IDEA / VS Code

### 部署（生产）
- **选项 1**：Render / Railway（免费层）
- **选项 2**：阿里云 / 腾讯云轻量服务器

---

## 项目结构

```
fitness-app/
├── backend/                          # Spring Boot 后端
│   ├── src/main/java/com/fitness/
│   │   ├── domain/                   # POJO 实体类
│   │   │   ├── Exercise.java
│   │   │   ├── Plan.java
│   │   │   ├── Workout.java
│   │   │   ├── Prescription.java
│   │   │   ├── User.java
│   │   │   ├── UserProfile.java
│   │   │   └── ExerciseFeedback.java
│   │   ├── mapper/                   # MyBatis Mapper 接口
│   │   │   ├── ExerciseMapper.java
│   │   │   ├── PlanMapper.java
│   │   │   ├── WorkoutMapper.java
│   │   │   └── UserMapper.java
│   │   ├── service/                  # 业务逻辑层
│   │   │   ├── ExerciseService.java
│   │   │   ├── PlanService.java
│   │   │   ├── PlanGenerator.java    # 核心生成器
│   │   │   └── UserService.java
│   │   ├── controller/               # REST API 控制器
│   │   │   ├── ExerciseController.java
│   │   │   ├── PlanController.java
│   │   │   └── UserController.java
│   │   ├── dto/                      # 数据传输对象
│   │   ├── config/                   # 配置类
│   │   └── FitnessApplication.java   # 启动类
│   ├── src/main/resources/
│   │   ├── application.yml           # 应用配置
│   │   ├── mapper/                   # MyBatis XML Mapper
│   │   │   ├── ExerciseMapper.xml
│   │   │   ├── PlanMapper.xml
│   │   │   └── WorkoutMapper.xml
│   │   ├── schema.sql                # 数据库表结构
│   │   ├── data/                     # 初始数据
│   │   │   └── exercises-dataset/    # 从 GitHub 拉取
│   │   └── import.sql                # 数据导入脚本（可选）
│   ├── src/test/java/                # 单元测试
│   └── pom.xml                       # Maven 依赖
│
├── frontend/                         # Vue 3 前端
│   ├── src/
│   │   ├── views/                    # 页面组件
│   │   │   ├── ExerciseDetail.vue    # Exercise 详情页
│   │   │   ├── UserProfile.vue       # 用户档案表单
│   │   │   ├── PlanView.vue          # 查看 8 周计划
│   │   │   ├── WorkoutToday.vue      # 今日 Workout 打卡
│   │   │   └── ExerciseList.vue      # Exercise 列表（浏览）
│   │   ├── components/               # 可复用组件
│   │   │   ├── ExerciseCard.vue
│   │   │   ├── WorkoutBlock.vue
│   │   │   └── FeedbackDialog.vue
│   │   ├── stores/                   # Pinia 状态管理
│   │   │   ├── user.js
│   │   │   ├── plan.js
│   │   │   └── workout.js
│   │   ├── router/                   # 路由配置
│   │   │   └── index.js
│   │   ├── api/                      # API 请求封装
│   │   │   ├── exercise.js
│   │   │   ├── plan.js
│   │   │   └── user.js
│   │   ├── App.vue
│   │   └── main.js
│   ├── public/
│   ├── package.json
│   └── vite.config.js
│
├── local-middleware/
│   └── docker-compose.yml            # PostgreSQL + Redis 容器配置
├── CONTEXT.md                        # 领域模型文档（已完成）
├── PROJECT.md                        # 本文件
├── PROGRESS.md                       # 进度追踪
└── CHANGELOG.md                      # 修改记录
```

---

## 数据库设计（主要表）

### 一期 MVP 表结构

```sql
-- Exercise（动作）
exercises
  - id (dataset 原始 ID, PK)
  - name
  - category
  - body_part
  - equipment
  - target
  - muscle_group
  - secondary_muscles (JSONB)
  - instruction_steps (JSONB)  -- 多语言
  - gif_url
  - image_url

-- User（用户）
users
  - id (PK)
  - username
  - email
  - created_at

-- UserProfile（用户档案）
user_profiles
  - id (PK)
  - user_id (FK)
  - fitness_level (ENUM)
  - goal (ENUM)
  - days_per_week (INT)
  - available_equipment (JSONB)
  - created_at

-- Plan（训练计划）
plans
  - id (PK)
  - user_id (FK)
  - profile_snapshot (JSONB)
  - status (ENUM)
  - start_date
  - end_date
  - parent_plan_id (FK, nullable)
  - created_at

-- Workout（训练日）
workouts
  - id (PK)
  - plan_id (FK)
  - day_number (INT)
  - focus (ENUM)
  - completed_at (nullable)

-- Prescription（动作处方）
prescriptions
  - id (PK)
  - workout_id (FK)
  - exercise_id (FK)
  - sequence (INT)
  - sets (INT)
  - reps (INT)
  - load (DECIMAL)
  - load_type (ENUM)
  - rpe (DECIMAL)

-- ExerciseFeedback（动作反馈）
exercise_feedbacks
  - id (PK)
  - workout_id (FK)
  - exercise_id (FK)
  - feedback_type (VARCHAR: TOO_EASY / JUST_RIGHT / TOO_HARD / HURT_<body_part>)
  - created_at
```

---

## API 设计（RESTful）

### Exercise 相关
```
GET    /api/exercises           # 列表（支持分页、筛选）
GET    /api/exercises/{id}      # 详情
GET    /api/exercises/search    # 搜索（按 name/body_part/equipment）
```

统一响应格式：
```json
{ "code": 0, "message": "success", "data": {} }
```

### User 相关
```
POST   /api/users/register      # 注册
POST   /api/users/login         # 登录
GET    /api/users/me            # 当前用户信息
POST   /api/users/profile       # 创建/更新档案
GET    /api/users/profile       # 获取档案
```

### Plan 相关
```
POST   /api/plans/generate      # 根据档案生成 Plan
GET    /api/plans               # 用户的所有 Plan
GET    /api/plans/{id}          # Plan 详情
GET    /api/plans/{id}/workouts # Plan 的所有 Workout
PUT    /api/plans/{id}/status   # 更新 Plan 状态（PAUSED/CANCELLED）
```

### Workout 相关
```
GET    /api/workouts/today      # 今日 Workout
POST   /api/workouts/{id}/complete        # 完成打卡
POST   /api/workouts/{id}/feedback        # 提交反馈
GET    /api/workouts/{id}/prescriptions   # Workout 的动作列表
```

---

## 开发路线图（垂直切片）

### 切片 1：数据导入 + Exercise 详情（1-2 天）
- [x] 初始化 Spring Boot 项目
- [x] 配置 MyBatis + PostgreSQL
- [x] 写 docker-compose.yml
- [x] 设计 exercises 表结构
- [x] 下载 exercises-dataset
- [x] 写数据导入器
- [x] 实现 Exercise Mapper + Service + Controller
- [x] 初始化 Vue 3.5.40 前端
- [x] 实现 Exercise 详情页骨架

### 切片 2：用户档案（1 天）
- [ ] 设计 users + user_profiles 表
- [ ] 实现 User/UserProfile Mapper + Service + Controller
- [ ] 前端：用户档案表单

### 切片 3：Plan 生成（2-3 天）
- [ ] 设计 plans + workouts + prescriptions 表
- [ ] 实现 Plan/Workout Mapper + Service
- [ ] **核心**：实现 PlanGenerator（简化版，硬编码 1 个模板）
- [ ] API：POST /api/plans/generate
- [ ] 前端：查看 Plan（8 周日历 + Workout 列表）

### 切片 4：Workout 打卡（1-2 天）
- [ ] 设计 exercise_feedbacks 表
- [ ] 实现 Workout 完成逻辑
- [ ] 实现 ExerciseFeedback Mapper + Service
- [ ] 前端：今日 Workout 页 + 打卡 + 反馈弹窗

---

## 参考资源

- **领域模型**：`CONTEXT.md`（40 条已决决策）
- **数据源**：[exercises-dataset](https://github.com/hasaneyldrm/exercises-dataset)
- **Spring Boot 文档**：https://spring.io/projects/spring-boot
- **MyBatis 文档**：https://mybatis.org/mybatis-3/
- **Vue 3 文档**：https://vuejs.org/
- **Element Plus**：https://element-plus.org/
