#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BACKEND_DIR="$ROOT_DIR/backend"
RUNTIME_DIR="$ROOT_DIR/.dev"
PID_FILE="$RUNTIME_DIR/backend.pid"
LOG_FILE="$RUNTIME_DIR/backend.log"
DEFAULT_JAVA_HOME="/Users/qiyee/baseEnv/jdk-21.0.7.jdk/Contents/Home"
DEFAULT_MAVEN_HOME="/Users/qiyee/baseEnv/apache-maven-3.9.9"
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
  if [[ -d "$DEFAULT_JAVA_HOME" ]]; then
    export JAVA_HOME="$DEFAULT_JAVA_HOME"
  elif command -v /usr/libexec/java_home >/dev/null 2>&1; then
    export JAVA_HOME="$(/usr/libexec/java_home -v 21 2>/dev/null || true)"
  fi

  if [[ -n "${JAVA_HOME:-}" ]]; then
    export PATH="$JAVA_HOME/bin:$PATH"
  fi

  if [[ -d "$DEFAULT_MAVEN_HOME" ]]; then
    export PATH="$DEFAULT_MAVEN_HOME/bin:$PATH"
  fi

  if ! command -v java >/dev/null 2>&1; then
    echo "Java is required but was not found. Install Java 21 or set JAVA_HOME to a Java 21+ JDK."
    return 1
  fi

  local java_major
  java_major="$(java -version 2>&1 | awk -F '[\".]' '/version/ {print $2; exit}')"
  if [[ -z "$java_major" || "$java_major" -lt 21 ]]; then
    echo "Java 21+ is required for the backend."
    java -version
    echo "Set JAVA_HOME to a Java 21+ JDK or install it at $DEFAULT_JAVA_HOME."
    return 1
  fi

  if ! command -v mvn >/dev/null 2>&1; then
    echo "Maven is required but was not found. Install Maven or add it to PATH."
    return 1
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
    {
      echo "Using Java: $(java -version 2>&1 | head -n 1)"
      echo "Using Maven: $(mvn -version | head -n 1)"
      echo "Starting backend on port $BACKEND_PORT"
    } >> "$LOG_FILE"
    nohup env SERVER_PORT="$BACKEND_PORT" mvn spring-boot:run -Dspring-boot.run.fork=false >> "$LOG_FILE" 2>&1 &
    echo "$!" > "$PID_FILE"
  )
  pid="$(read_pid)"

  for _ in {1..20}; do
    if ! is_running "$pid"; then
      rm -f "$PID_FILE"
      echo "Backend failed to start. Log: $LOG_FILE"
      tail -60 "$LOG_FILE"
      return 1
    fi

    if grep -q "Started FitnessApplication" "$LOG_FILE"; then
      echo "Backend started: pid=$pid, url=http://localhost:$BACKEND_PORT/api"
      echo "Log: $LOG_FILE"
      return 0
    fi

    sleep 1
  done

  echo "Backend is still starting: pid=$pid, url=http://localhost:$BACKEND_PORT/api"
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
