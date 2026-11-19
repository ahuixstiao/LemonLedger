#!/usr/bin/env bash
# LemonLedger 数据库备份脚本（在宿主机上跑，git bash / WSL 均可）
#
# 用法：
#   ./deploy/backup-mysql.sh                # 备份一次
#   ./deploy/backup-mysql.sh --keep 30      # 备份一次，只保留最近 30 个文件
#   ./deploy/backup-mysql.sh --restore backups/ledger-20260101-023000.sql.gz
#
# 依赖：Docker Desktop 已启动，mysql_container 在运行。
# 备份文件：<repo>/backups/ledger-YYYYmmdd-HHMMSS.sql.gz
# 日志：    <repo>/backups/backup.log
#
# 说明：这台机器的 git bash 没有 gzip，所以压缩/解压/校验都借 mysql 容器里的 gzip 完成，
#       宿主机只负责接收字节流，不再需要额外装工具。

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
BACKUP_DIR="${BACKUP_DIR:-$REPO_DIR/backups}"
CONTAINER="${MYSQL_CONTAINER:-mysql_container}"
DATABASE="${MYSQL_DATABASE:-ledger}"
KEEP="${BACKUP_KEEP:-14}"
LOG_FILE="$BACKUP_DIR/backup.log"

mkdir -p "$BACKUP_DIR"

log() {
  local line
  line="$(date '+%Y-%m-%d %H:%M:%S') $*"
  echo "$line"
  echo "$line" >> "$LOG_FILE"
}

fail() {
  log "FAILED: $*"
  exit 1
}

restore() {
  local file="$1"
  [ -f "$file" ] || fail "找不到备份文件：$file"
  log "开始恢复 $file → $DATABASE"
  docker exec -i "$CONTAINER" sh -c \
    'gzip -dc | exec mysql -uroot -p"$MYSQL_ROOT_PASSWORD" --default-character-set=utf8mb4 '"$DATABASE" \
    < "$file" || fail "恢复失败：$file"
  log "恢复完成：$file"
}

# ---- 参数 ----
RESTORE_FILE=""
while [ $# -gt 0 ]; do
  case "$1" in
    --keep) KEEP="$2"; shift 2 ;;
    --restore) RESTORE_FILE="$2"; shift 2 ;;
    -h|--help) sed -n '2,18p' "$0"; exit 0 ;;
    *) fail "未知参数：$1" ;;
  esac
done

if [ -n "$RESTORE_FILE" ]; then
  restore "$RESTORE_FILE"
  exit 0
fi

# ---- 检查容器 ----
docker inspect "$CONTAINER" >/dev/null 2>&1 || fail "容器 $CONTAINER 不存在，先启动 docker compose"
RUNNING="$(docker inspect -f '{{.State.Running}}' "$CONTAINER" 2>/dev/null || echo false)"
[ "$RUNNING" = "true" ] || fail "容器 $CONTAINER 没在运行"

# ---- 导出（dump 与 gzip 都在容器里，宿主机只落字节） ----
STAMP="$(date '+%Y%m%d-%H%M%S')"
TMP_FILE="$BACKUP_DIR/.ledger-$STAMP.sql.gz.part"
FINAL_FILE="$BACKUP_DIR/ledger-$STAMP.sql.gz"

log "开始备份 $DATABASE（容器 $CONTAINER）"
# --single-transaction 不锁表；--quick 不把整表读进内存
if ! docker exec "$CONTAINER" sh -c \
  'exec mysqldump -uroot -p"$MYSQL_ROOT_PASSWORD" \
     --single-transaction --quick --routines --triggers \
     --default-character-set=utf8mb4 '"$DATABASE"' | gzip -9' \
  > "$TMP_FILE"; then
  rm -f "$TMP_FILE"
  fail "mysqldump 执行失败"
fi

# ---- 校验：gzip 完整性 + 文件大小 + 里面有 job 建表语句 ----
docker exec -i "$CONTAINER" gzip -t < "$TMP_FILE" 2>/dev/null \
  || { rm -f "$TMP_FILE"; fail "备份文件损坏（gzip 校验失败）"; }

SIZE="$(wc -c < "$TMP_FILE" | tr -d ' ')"
[ "$SIZE" -gt 10240 ] || { rm -f "$TMP_FILE"; fail "备份文件太小（${SIZE} 字节），疑似失败"; }

TABLE_COUNT="$(docker exec -i "$CONTAINER" sh -c 'gzip -dc | grep -c "^CREATE TABLE"' < "$TMP_FILE" || true)"
[ "${TABLE_COUNT:-0}" -gt 0 ] || { rm -f "$TMP_FILE"; fail "备份里没有建表语句，疑似失败"; }

mv "$TMP_FILE" "$FINAL_FILE"
log "备份完成：$(basename "$FINAL_FILE")（${SIZE} 字节，${TABLE_COUNT} 张表）"

# ---- 清理旧备份 ----
COUNT=0
while IFS= read -r old; do
  rm -f "$old"
  COUNT=$((COUNT + 1))
done < <(ls -1t "$BACKUP_DIR"/ledger-*.sql.gz 2>/dev/null | tail -n +$((KEEP + 1)))
[ "$COUNT" -gt 0 ] && log "清理旧备份 $COUNT 个（保留 $KEEP 个）"

log "全部完成"
