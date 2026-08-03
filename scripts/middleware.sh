#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
MIDDLEWARE_DIR="$ROOT_DIR/local-middleware"

usage() {
  echo "Usage: scripts/middleware.sh {start|stop|restart|status|logs}"
}

compose() {
  cd "$MIDDLEWARE_DIR"
  docker compose "$@"
}

case "${1:-}" in
  start) compose up -d ;;
  stop) compose down ;;
  restart) compose down; compose up -d ;;
  status) compose ps ;;
  logs) compose logs -f ;;
  *) usage; exit 1 ;;
esac
