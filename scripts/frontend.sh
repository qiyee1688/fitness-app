#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
FRONTEND_DIR="$ROOT_DIR/frontend"
RUNTIME_DIR="$ROOT_DIR/.dev"
PID_FILE="$RUNTIME_DIR/frontend.pid"
LOG_FILE="$RUNTIME_DIR/frontend.log"
FRONTEND_PORT="${FRONTEND_PORT:-5173}"

mkdir -p "$RUNTIME_DIR"

usage() {
  echo "Usage: scripts/frontend.sh {start|stop|restart|status|logs|build}"
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
  command -v lsof >/dev/null 2>&1 && lsof -nP -iTCP:"$FRONTEND_PORT" -sTCP:LISTEN >/dev/null 2>&1
}

start_frontend() {
  local pid
  pid="$(read_pid || true)"
  if is_running "$pid"; then
    echo "Frontend already running: pid=$pid, url=http://localhost:$FRONTEND_PORT"
    return 0
  fi

  if is_port_busy; then
    echo "Port $FRONTEND_PORT is already in use by another process."
    echo "Run scripts/frontend.sh status, stop that process, or set FRONTEND_PORT=5174."
    return 1
  fi

  : > "$LOG_FILE"
  (
    cd "$FRONTEND_DIR"
    exec npm run dev -- --port "$FRONTEND_PORT" --strictPort
  ) >> "$LOG_FILE" 2>&1 &
  pid=$!
  echo "$pid" > "$PID_FILE"
  echo "Frontend starting: pid=$pid, url=http://localhost:$FRONTEND_PORT"
  echo "Log: $LOG_FILE"
}

stop_frontend() {
  local pid
  pid="$(read_pid || true)"
  if ! is_running "$pid"; then
    rm -f "$PID_FILE"
    echo "Frontend is not running."
    return 0
  fi

  kill "$pid"
  for _ in {1..20}; do
    if ! is_running "$pid"; then
      rm -f "$PID_FILE"
      echo "Frontend stopped."
      return 0
    fi
    sleep 1
  done

  echo "Frontend did not stop within 20s. Check pid=$pid and log=$LOG_FILE"
  return 1
}

status_frontend() {
  local pid
  pid="$(read_pid || true)"
  if is_running "$pid"; then
    echo "Frontend running: pid=$pid, url=http://localhost:$FRONTEND_PORT"
  else
    rm -f "$PID_FILE"
    echo "Frontend not running."
  fi

  if command -v lsof >/dev/null 2>&1; then
    lsof -nP -iTCP:"$FRONTEND_PORT" -sTCP:LISTEN || true
  fi
}

logs_frontend() {
  touch "$LOG_FILE"
  tail -f "$LOG_FILE"
}

build_frontend() {
  cd "$FRONTEND_DIR"
  npm run build
}

case "${1:-}" in
  start) start_frontend ;;
  stop) stop_frontend ;;
  restart) stop_frontend; start_frontend ;;
  status) status_frontend ;;
  logs) logs_frontend ;;
  build) build_frontend ;;
  *) usage; exit 1 ;;
esac
