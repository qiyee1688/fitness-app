# Fitness App - Local Middleware

统一管理项目所需的所有中间件（PostgreSQL、Redis 等）。

## 启动所有中间件

```bash
cd local-middleware
docker compose up -d
```

## 停止所有中间件

```bash
docker compose down
```

## 查看中间件状态

```bash
docker compose ps
```

## 查看日志

```bash
# 查看所有中间件日志
docker compose logs -f

# 查看特定中间件日志
docker compose logs -f postgres
docker compose logs -f redis
```

## 中间件列表

### PostgreSQL
- **容器名**: `fitness-postgres`
- **容器端口**: `5432`
- **宿主机端口**: `15432`
- **数据库**: `fitness_db`
- **用户**: `fitness_user`
- **密码**: `fitness_pass`
- **连接字符串**: `jdbc:postgresql://localhost:15432/fitness_db`

### Redis
- **容器名**: `fitness-redis`
- **容器端口**: `6379`
- **宿主机端口**: `16379`
- **持久化**: AOF 模式
- **连接**: `redis://localhost:16379`

## 数据持久化

所有数据存储在 Docker 卷中：
- `postgres_data` - PostgreSQL 数据
- `redis_data` - Redis 数据

## 清理数据（谨慎操作）

```bash
# 停止并删除容器和卷
docker compose down -v
```

## 进入容器

```bash
# 进入 PostgreSQL 容器
docker exec -it fitness-postgres psql -U fitness_user -d fitness_db

# 进入 Redis 容器
docker exec -it fitness-redis redis-cli
```
# fitness-app
# fitness-app
# fitness-app
# fitness-app
