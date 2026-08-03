#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BACKEND_DIR="$ROOT_DIR/backend"
RUNTIME_DIR="$ROOT_DIR/.dev"
PID_FILE="$RUNTIME_DIR/backend.pid"
LOG_FILE="$RUNTIME_DIR/backend.log"
DEFAULT_JAVA_HOME="/Users/qiyee/baseEnv/jdk-21.0.7.jdk/Contents/Home"
BACKEND_PORT="${BACKEND_PORT:-8080}"

mkdir -p "$RUNTIME_DIR"

usage() {
  echo "Usage: scripts/backend.sh {start|stop|restart|status|logs|test|seed}"
}

read_pid() {
  if [[ -f "$PID_FILE" ]]; then
    tr -d '[:space:]' < "$PID_FILE"
  fi
}

is_running() {
  local pid="${1:-}"
  [[ -n "$pid" ]] && kill -0 "$pid" >/dev/null 2>&1
}

is_port_busy() {
  command -v lsof >/dev/null 2>&1 && lsof -nP -iTCP:"$BACKEND_PORT" -sTCP:LISTEN >/dev/null 2>&1
}

configure_java() {
  if [[ -z "${JAVA_HOME:-}" && -d "$DEFAULT_JAVA_HOME" ]]; then
    export JAVA_HOME="$DEFAULT_JAVA_HOME"
  fi

  if [[ -n "${JAVA_HOME:-}" ]]; then
    export PATH="$JAVA_HOME/bin:$PATH"
  fi
}

start_backend() {
  local pid
  pid="$(read_pid || true)"
  if is_running "$pid"; then
    echo "Backend already running: pid=$pid, url=http://localhost:$BACKEND_PORT/api"
    return 0
  fi

  if is_port_busy; then
    echo "Port $BACKEND_PORT is already in use by another process."
    echo "Run scripts/backend.sh status, stop that process, or set BACKEND_PORT=8081."
    return 1
  fi

  configure_java
  : > "$LOG_FILE"
  (
    cd "$BACKEND_DIR"
    exec env SERVER_PORT="$BACKEND_PORT" mvn spring-boot:run
  ) >> "$LOG_FILE" 2>&1 &
  pid=$!
  echo "$pid" > "$PID_FILE"
  echo "Backend starting: pid=$pid, url=http://localhost:$BACKEND_PORT/api"
  echo "Log: $LOG_FILE"
}

stop_backend() {
  local pid
  pid="$(read_pid || true)"
  if ! is_running "$pid"; then
    rm -f "$PID_FILE"
    echo "Backend is not running."
    return 0
  fi

  kill "$pid"
  for _ in {1..30}; do
    if ! is_running "$pid"; then
      rm -f "$PID_FILE"
      echo "Backend stopped."
      return 0
    fi
    sleep 1
  done

  echo "Backend did not stop within 30s. Check pid=$pid and log=$LOG_FILE"
  return 1
}

status_backend() {
  local pid
  pid="$(read_pid || true)"
  if is_running "$pid"; then
    echo "Backend running: pid=$pid, url=http://localhost:$BACKEND_PORT/api"
  else
    rm -f "$PID_FILE"
    echo "Backend not running."
  fi

  if command -v lsof >/dev/null 2>&1; then
    lsof -nP -iTCP:"$BACKEND_PORT" -sTCP:LISTEN || true
  fi
}

logs_backend() {
  touch "$LOG_FILE"
  tail -f "$LOG_FILE"
}

test_backend() {
  configure_java
  cd "$BACKEND_DIR"
  mvn test
}

seed_backend() {
  configure_java
  cd "$BACKEND_DIR"
  mvn spring-boot:run -Dspring-boot.run.arguments=--fitness.seed.exercises.enabled=true
}

case "${1:-}" in
  start) start_backend ;;
  stop) stop_backend ;;
  restart) stop_backend; start_backend ;;
  status) status_backend ;;
  logs) logs_backend ;;
  test) test_backend ;;
  seed) seed_backend ;;
  *) usage; exit 1 ;;
esac
