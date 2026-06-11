# Document Processing Pipeline

**App B — UBS Document Processing &
Compliance Reporting**

Java 21 / Spring Boot 3.4 / Apache Camel 4.8
application that processes incoming banking
documents (PDF/CSV) through a compliance
validation pipeline. Targets **RHEL 10**.

## Architecture

```
 /mnt/shared-docs/  (NFS)
        |
        v
 /opt/ubs/incoming-docs/  <-- Camel watches
        |
        v
 [File Lock] /opt/ubs/locks/*.lock
        |
        v
 [libpdf_ubs.so]  (native lib — RPM)
        |
        v
 [SOAP Validation]  --> 10.192.4.47:8080
        |
        v
 [Redis Cache]  --> settle-cache-01:6379
        |
        v
 [Oracle Audit]  --> ora-auditdb-01:1521
        |
        v
 /opt/ubs/processed/   (output)
 /opt/ubs/reports/     (compliance reports)
```

## Key Dependencies

| Dependency | Version | Notes |
|---|---|---|
| Java | 21 (OpenJDK) | RHEL 10 LTS |
| Spring Boot | 3.4.1 | LTS |
| Apache Camel | 4.8.2 | File routing |
| OpenSSL | 3.x | RHEL 10 default |
| libpdf_ubs.so | TBD | Recompile needed |
| Redis | 7.x | On-prem, no TLS |
| Oracle | 19c+ | Audit database |

## Prerequisites (RHEL 10)

- `java-21-openjdk` and
  `java-21-openjdk-devel`
- `nfs-utils` for NFS mounts
- `redis` CLI for health checks
- `oracle-instantclient-basic` for
  sqlplus
- `ubs-libpdf` RPM (recompiled for
  RHEL 10 / glibc 2.39+)
- Network path to Oracle, Redis, SOAP,
  Splunk, NFS endpoints

## Building

Requires Java 21:

```bash
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk
mvn clean package -DskipTests
```

Run tests:

```bash
mvn test
```

## Deployment

Deployed via Puppet to RHEL 10 hosts.
See `deploy/manifests/init.pp`.

```bash
# Systemd service
systemctl start doc-pipeline
systemctl status doc-pipeline
journalctl -u doc-pipeline -f
```

## Configuration

All runtime configuration is externalized
in `src/main/resources/application.properties`.
Override per-environment via:

- Spring profiles:
  `-Dspring.profiles.active=production`
- Environment variables:
  `ORACLE_DB_PASSWORD`, etc.
- System properties:
  `-Ddocpipeline.redis.host=...`

Key property prefixes:

| Prefix | Controls |
|---|---|
| `docpipeline.dirs.*` | Filesystem paths |
| `docpipeline.redis.*` | Redis connection |
| `docpipeline.compliance.*` | SOAP URL |
| `docpipeline.native.*` | Native lib path |
| `docpipeline.cache.*` | Cache settings |
| `oracle.*` | Oracle DB connection |

## RHEL 7 → RHEL 10 Changes Summary

**Runtime upgrades:**
- Java 1.8.0 → 21 (OpenJDK LTS)
- Spring Boot 2.7.18 → 3.4.1
- Apache Camel 3.14.10 → 4.8.2
- ojdbc8 → ojdbc11
- javax.* → jakarta.* namespace

**Code quality:**
- Hardcoded config → application.properties
- @Autowired field → constructor injection
- java.util.Date → java.time.Instant
- Exception handling narrowed + logged
- Long methods extracted for readability

**Deployment:**
- Puppet: yum → dnf, Java 8 → 21 packages
- systemd: hardening directives added
- OpenSSL: 1.0.2k pin removed (3.x default)

See `docs/rhel10-migration-checklist.md` for
the full migration checklist including items
requiring team coordination.

## Project Structure

```
├── pom.xml
├── src/main/java/com/ubs/docpipeline/
│   ├── Application.java
│   ├── config/
│   │   ├── CamelRouteConfig.java
│   │   ├── OracleConfig.java
│   │   ├── RedisConfig.java
│   │   └── SoapClientConfig.java
│   ├── model/
│   │   ├── AuditRecord.java
│   │   ├── ComplianceResult.java
│   │   └── ProcessedDocument.java
│   ├── processor/
│   │   └── IncomingDocProcessor.java
│   ├── route/
│   │   └── DocumentRoute.java
│   └── service/
│       ├── AuditService.java
│       ├── ComplianceValidationService.java
│       ├── DocumentProcessorService.java
│       ├── FileLockService.java
│       └── NativePdfService.java
├── src/main/resources/
│   ├── application.properties
│   └── wsdl/compliance-validation.wsdl
├── deploy/
│   ├── manifests/  (Puppet)
│   │   ├── init.pp
│   │   ├── packages.pp
│   │   ├── directories.pp
│   │   ├── nfs_mounts.pp
│   │   ├── crontab.pp
│   │   ├── rsyslog.pp
│   │   ├── openssl.pp
│   │   └── service.pp
│   ├── scripts/
│   │   ├── reconciliation.sh
│   │   └── check-oracle.sh
│   └── templates/
│       └── rsyslog.conf.erb
├── docs/
│   ├── rhel10-dependency-matrix.md
│   └── rhel10-migration-checklist.md
└── vm-metadata/
    ├── tanium-scan-report.txt
    └── migration-notes.md
```
