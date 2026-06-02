# Document Processing Pipeline

**App B — UBS Document Processing &
Compliance Reporting**

Java 8 / Spring Boot 2.7 / Apache Camel
application that processes incoming banking
documents (PDF/CSV) through a compliance
validation pipeline on RHEL 7.

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
 [libpdf_ubs.so]  (native RHEL 7 lib)
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
|------------|---------|-------|
| Java | 1.8.0 | OpenJDK on RHEL 7 |
| Spring Boot | 2.7.18 | EOL Nov 2023 |
| Apache Camel | 3.14.10 | File routing |
| OpenSSL | 1.0.2k | EOL, pinned |
| libpdf_ubs.so | 2.3.1 | Custom RPM |
| Redis | 4.0 | On-prem, no TLS |
| Oracle | 19c | Audit database |

## Why Containerization Is Impractical

1. **Native `.so` library** — `libpdf_ubs.so`
   compiled for RHEL 7 x86_64, binary-only
   RPM with no source code. Cannot be included
   in a container image without recompilation.

2. **Hardcoded SOAP endpoint** — Compliance
   service at `10.192.4.47:8080` with no
   migration plan. Requires direct L3 network
   path not available from K8s pod network.

3. **Filesystem-based IPC** — File locking in
   `/opt/ubs/locks/`, NFS mounts for
   inter-system file exchange, directory
   watching for incoming documents.

4. **OS-level integration** — 12 cron jobs,
   rsyslog to Splunk, systemd service,
   OpenSSL version pinning.

5. **Sticky sessions** — Host-pinned Redis
   on local subnet for processing state
   cache. No Redis Cluster support.

## Recommended Path: Lift and Shift to RHEL 8

See `vm-metadata/migration-notes.md` for the
full migration checklist.

### What Devin Can Automate

- Upgrade Java 8 → 17
- Upgrade Spring Boot 2.7 → 3.x
- Upgrade Apache Camel 3.14 → 4.x
- Update Maven dependencies
- Generate new Puppet manifests for RHEL 8
- Update systemd and crontab configs

### What Requires Human Coordination

- `libpdf_ubs.so` recompilation (RPM team)
- SOAP endpoint network path verification
- NFS mount migration
- OpenSSL 3.x upgrade (after libpdf rebuild)
- Redis upgrade coordination

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
└── vm-metadata/
    ├── tanium-scan-report.txt
    └── migration-notes.md
```

## Building

```bash
mvn clean package -DskipTests
```

## Deployment

Deployed via Puppet to RHEL 7 hosts.
See `deploy/manifests/init.pp`.
