# Fitness App Backend

Spring Boot 3.5.16 + Java 21 + MyBatis + PostgreSQL

## 快速启动

### 1. 启动 PostgreSQL
```bash
../scripts/middleware.sh start
```

### 2. 构建项目
```bash
cd backend
mvn clean install
```

### 3. 运行应用
```bash
../scripts/backend.sh start
```

应用将在 `http://localhost:8080/api` 启动。

### 常用开发命令

```bash
../scripts/backend.sh status
../scripts/backend.sh restart
../scripts/backend.sh logs
../scripts/backend.sh test
```

后端脚本会优先使用项目固定的 Java 21.0.7 和 Maven 3.9.9 路径。若启动失败，`start` 会返回非 0，并直接打印 `.dev/backend.log` 的尾部。

## 数据库初始化

数据库表结构会在容器启动时自动创建（通过 `schema.sql`）。

### 导入 exercises-dataset

首次导入时运行：

```bash
../scripts/backend.sh seed
```

导入器会在 `exercises` 已有数据时跳过，避免重复写入。

## API 端点

### Exercise 相关

- `GET /api/exercises` - 获取所有动作（支持分页和筛选）
  - 参数：`page`, `pageSize`, `category`, `bodyPart`, `equipment`, `muscleGroup`
- `GET /api/exercises/{id}` - 获取单个动作详情
- `GET /api/exercises/search?keyword={keyword}` - 搜索动作

## 技术栈

- **Java**: 21 LTS（目标 21.0.7）
- **Spring Boot**: 3.5.16
- **MyBatis Spring Boot Starter**: 3.0.5
- **PostgreSQL**: 15
- **Lombok**: 减少样板代码

## 响应格式

所有 REST API 返回统一 JSON：

```json
{ "code": 0, "message": "success", "data": {} }
```

## 项目结构

```
backend/
├── src/main/java/com/fitness/
│   ├── domain/          # POJO 实体类
│   ├── mapper/          # MyBatis Mapper 接口
│   ├── service/         # 业务逻辑层
│   ├── controller/      # REST API 控制器
│   ├── dto/             # 数据传输对象
│   ├── config/          # 配置类（TypeHandler）
│   └── FitnessApplication.java
├── src/main/resources/
│   ├── application.yml
│   ├── schema.sql
│   └── mapper/          # MyBatis XML Mapper
└── pom.xml
```

## 下一步

- 为 Exercise API 补测试
- 前端 Exercise 详情页视觉走查
