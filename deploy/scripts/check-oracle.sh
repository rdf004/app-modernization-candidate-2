#!/bin/bash
# Oracle audit DB connectivity check
# Runs every 15 minutes via crontab

ORACLE_HOST="ora-auditdb-01.internal"
ORACLE_SID="UBSAUDIT"
ORACLE_USER="doc_pipeline"

sqlplus -s \
    "${ORACLE_USER}@//${ORACLE_HOST}" \
    ":1521/${ORACLE_SID}" \
    <<< "SELECT 1 FROM DUAL; EXIT;" \
    > /dev/null 2>&1

if [ $? -ne 0 ]; then
    logger -p local0.err \
        "Oracle audit DB unreachable: " \
        "${ORACLE_HOST}:1521/${ORACLE_SID}"
    echo "$(date) FAIL: Oracle unreachable"
    exit 1
fi

echo "$(date) OK: Oracle reachable"
