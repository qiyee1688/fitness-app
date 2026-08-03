# fitness-app

## 开发服务脚本

项目根目录提供统一脚本，便于开发人员启动、停止、重启和查看服务状态。

```bash
# 启动 PostgreSQL/Redis + Backend + Frontend
scripts/dev.sh start

# 查看全栈状态
scripts/dev.sh status

# 重启全栈
scripts/dev.sh restart

# 停止全栈
scripts/dev.sh stop
```

单独操作某个服务：

```bash
scripts/middleware.sh start|stop|restart|status|logs
scripts/backend.sh start|stop|restart|status|logs|test|seed
scripts/frontend.sh start|stop|restart|status|logs|build
```

默认端口：

- Frontend: `http://localhost:5173`
- Backend: `http://localhost:8080/api`
- PostgreSQL: `localhost:15432`
- Redis: `localhost:16379`

运行日志和 PID 文件会写入 `.dev/`，该目录不会提交到 Git。

如需临时换端口，可在命令前设置 `BACKEND_PORT` 或 `FRONTEND_PORT`，例如 `FRONTEND_PORT=5174 scripts/frontend.sh start`。

后端脚本会优先使用项目固定的 Java 21.0.7 和 Maven 3.9.9 路径，并在 `.dev/backend.log` 开头打印实际使用的 Java / Maven 版本。
