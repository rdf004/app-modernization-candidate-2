# Migration Assessment Notes

## Host: doc-proc-01.internal.ubs.com

### Assessment Summary

**Recommendation: Lift and Shift to RHEL 8**

Containerization is **not practical** for this
application due to deep OS-level dependencies,
native library bindings, filesystem-based IPC,
and network constraints that cannot be
replicated in a Kubernetes environment.

### Blocking Factors for Containerization

#### 1. Native Library Dependency

`libpdf_ubs.so` (v2.3.1) is a custom UBS PDF
parsing library:

- Compiled specifically for RHEL 7 x86_64
- Linked against glibc 2.17 and OpenSSL 1.0.2k
- Distributed as binary-only RPM (no source)
- Cannot be included in a container image
  without recompilation for target OS
- RPM team must recompile for RHEL 8

#### 2. Hardcoded SOAP Endpoint

The compliance validation service at
`http://10.192.4.47:8080/compliance-api/v1/validate`
is:

- Owned by Compliance Engineering team
- No migration or containerization planned
- Requires direct L3 network path from host
- Not accessible from cloud VPC or container
  network without custom routing (not approved
  by Network Engineering)

#### 3. Filesystem Dependencies

- Watches `/opt/ubs/incoming-docs/` for files
- Uses `.lock` files in `/opt/ubs/locks/`
  for inter-process coordination
- Assumes co-located instances share filesystem
- 3 NFS mounts for inter-system file exchange
- Cannot replicate with K8s PersistentVolumes
  due to lock semantics

#### 4. OS-Level Integration

- 12 cron jobs for batch processing, health
  checks, and maintenance
- Rsyslog forwarding to Splunk at OS level
- Systemd service management
- OpenSSL 1.0.2k pinned (EOL) — required
  by native library

#### 5. Sticky Sessions + Host-Pinned Redis

- Redis at settle-cache-01.internal:6379
  stores intermediate processing state
- Application assumes sticky sessions to
  same Redis instance
- No Redis Cluster or Sentinel — single
  instance on local subnet

### RHEL 8 Migration Checklist

- [ ] Recompile libpdf_ubs.so for RHEL 8
      (RPM team — no source available,
       requires build team coordination)
- [ ] Upgrade OpenSSL 1.0.2k to 3.x
      (must happen after libpdf recompile)
- [ ] Upgrade Java 8 to 17 (LTS)
- [ ] Upgrade Spring Boot 2.7 to 3.x
- [ ] Upgrade Apache Camel 3.14 to 4.x
- [ ] Update Puppet manifests for RHEL 8
      package names and systemd syntax
- [ ] Verify network connectivity to
      10.192.4.47:8080 from new host
- [ ] Migrate NFS mount configs
- [ ] Update crontab entries if needed
- [ ] Update rsyslog config for RHEL 8
- [ ] Upgrade Redis client (Redis 4.0 EOL)
- [ ] Test Oracle connectivity from new host
- [ ] Coordinate with Compliance team re:
      SOAP endpoint network path
- [ ] Full regression test of PDF processing
      with recompiled libpdf_ubs.so

### Risk Assessment

| Risk | Severity | Owner |
|------|----------|-------|
| libpdf recompile fails | High | RPM Team |
| SOAP network path | High | Network Eng |
| OpenSSL upgrade breaks deps | Med | Platform |
| NFS mount perf on new host | Low | Storage |
| Redis version mismatch | Low | Platform |
