#!/bin/bash
# =========================================
# Nightly Batch Reconciliation Script
# =========================================
#
# Triggered via OS-level crontab at 2:00 AM.
# NOT managed by Spring Scheduler.
#
# Reconciles processed documents against
# Oracle audit log entries and generates
# a daily compliance report.
#
# Dependencies:
# - Oracle Instant Client (sqlplus)
# - Network path to ora-auditdb-01.internal
# - Redis CLI for cache verification
# - NFS mount at /mnt/shared-docs/
# =========================================

set -euo pipefail

PROCESSED_DIR="/opt/ubs/processed"
REPORTS_DIR="/opt/ubs/reports"
LOCK_FILE="/opt/ubs/locks/reconcile.lock"
ORACLE_HOST="ora-auditdb-01.internal"
ORACLE_SID="UBSAUDIT"
ORACLE_USER="doc_pipeline"
REDIS_HOST="settle-cache-01.internal"
DATE=$(date +%Y-%m-%d)
LOG_PREFIX="[RECONCILE ${DATE}]"

echo "${LOG_PREFIX} Starting reconciliation"

# Acquire reconciliation lock
if [ -f "${LOCK_FILE}" ]; then
    PID=$(cat "${LOCK_FILE}")
    if kill -0 "${PID}" 2>/dev/null; then
        echo "${LOG_PREFIX} Already running"
        exit 1
    fi
fi
echo $$ > "${LOCK_FILE}"
trap 'rm -f ${LOCK_FILE}' EXIT

# Count processed files
PROC_COUNT=$(find "${PROCESSED_DIR}" \
    -type f -newermt "${DATE}" | wc -l)
echo "${LOG_PREFIX} Files processed: " \
    "${PROC_COUNT}"

# Query Oracle audit log for today
AUDIT_COUNT=$(sqlplus -s \
    "${ORACLE_USER}@//${ORACLE_HOST}" \
    ":1521/${ORACLE_SID}" <<EOF
SET HEADING OFF FEEDBACK OFF
SELECT COUNT(*)
FROM audit_log
WHERE TRUNC(created_at) = TRUNC(SYSDATE);
EXIT;
EOF
)

echo "${LOG_PREFIX} Audit entries: " \
    "${AUDIT_COUNT}"

# Verify Redis cache state
REDIS_KEYS=$(redis-cli \
    -h "${REDIS_HOST}" \
    KEYS "docpipeline:state:*" \
    | wc -l)
echo "${LOG_PREFIX} Redis cached: " \
    "${REDIS_KEYS}"

# Generate reconciliation report
REPORT="${REPORTS_DIR}/reconcile-${DATE}.txt"
cat > "${REPORT}" <<EOF
========================================
Daily Reconciliation Report — ${DATE}
========================================
Files Processed:   ${PROC_COUNT}
Audit Log Entries: ${AUDIT_COUNT}
Redis Cache Keys:  ${REDIS_KEYS}
NFS Mount Status:  $(mountpoint -q \
    /mnt/shared-docs && echo "OK" \
    || echo "UNMOUNTED")
Oracle Status:     $(sqlplus -s \
    "${ORACLE_USER}@//${ORACLE_HOST}" \
    ":1521/${ORACLE_SID}" \
    <<< "SELECT 1 FROM DUAL; EXIT;" \
    > /dev/null 2>&1 \
    && echo "OK" || echo "UNREACHABLE")
SOAP Endpoint:     $(curl -sf \
    http://10.192.4.47:8080\
/compliance-api/v1/health \
    > /dev/null 2>&1 \
    && echo "OK" || echo "UNREACHABLE")
========================================
EOF

echo "${LOG_PREFIX} Report: ${REPORT}"

# Check for discrepancies
if [ "${PROC_COUNT}" != "${AUDIT_COUNT}" ]; then
    echo "${LOG_PREFIX} MISMATCH detected"
    logger -p local0.err \
        "Reconciliation mismatch: " \
        "files=${PROC_COUNT} " \
        "audit=${AUDIT_COUNT}"
fi

# Archive old reports
find "${REPORTS_DIR}" \
    -name "reconcile-*.txt" \
    -mtime +90 \
    -exec mv {} /mnt/doc-archive/ \;

echo "${LOG_PREFIX} Reconciliation complete"
