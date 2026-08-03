#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

usage() {
  echo "Usage: scripts/dev.sh {start|stop|restart|status|logs|test|build}"
}

case "${1:-}" in
  start)
    "$ROOT_DIR/scripts/middleware.sh" start
    "$ROOT_DIR/scripts/backend.sh" start
    "$ROOT_DIR/scripts/frontend.sh" start
    ;;
  stop)
    "$ROOT_DIR/scripts/frontend.sh" stop
    "$ROOT_DIR/scripts/backend.sh" stop
    "$ROOT_DIR/scripts/middleware.sh" stop
    ;;
  restart)
    "$ROOT_DIR/scripts/dev.sh" stop
    "$ROOT_DIR/scripts/dev.sh" start
    ;;
  status)
    "$ROOT_DIR/scripts/middleware.sh" status
    "$ROOT_DIR/scripts/backend.sh" status
    "$ROOT_DIR/scripts/frontend.sh" status
    ;;
  logs)
    echo "Backend log: $ROOT_DIR/.dev/backend.log"
    echo "Frontend log: $ROOT_DIR/.dev/frontend.log"
    echo "Use scripts/backend.sh logs or scripts/frontend.sh logs to follow one service."
    ;;
  test)
    "$ROOT_DIR/scripts/backend.sh" test
    ;;
  build)
    "$ROOT_DIR/scripts/frontend.sh" build
    ;;
  *)
    usage
    exit 1
    ;;
esac
