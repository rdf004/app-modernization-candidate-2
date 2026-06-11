# RHEL 7 → RHEL 10 Migration Checklist

**Application**: UBS Document Processing Pipeline
**Source**: RHEL 7.9 (doc-proc-01.internal)
**Target**: RHEL 10

---

## Completed (Automated)

- [x] Runtime: OpenJDK 1.8.0 → 21
- [x] Framework: Spring Boot 2.7.18 → 3.4.1
- [x] Framework: Apache Camel 3.14.10 → 4.8.2
- [x] Namespace: javax.* → jakarta.*
  (persistence, transaction, annotation)
- [x] JDBC: ojdbc8 21.1 → ojdbc11 23.6
- [x] JAX-WS: jaxws-api 2.3.1 →
  jakarta.xml.ws-api 4.0.2
- [x] JPA dialect: Oracle12cDialect →
  OracleDialect (Hibernate 6)
- [x] Properties: spring.redis.* →
  spring.data.redis.* (Spring Boot 3)
- [x] Config externalized: all hardcoded
  IPs, hosts, paths, ports moved to
  application.properties with @Value
- [x] Constructor injection: replaced all
  @Autowired field injection
- [x] Date/time: java.util.Date → Instant
  (AuditRecord, ProcessedDocument)
- [x] Exception handling: narrowed catch
  blocks, added logging to swallowed
  exceptions
- [x] Method extraction: large process()
  method split into focused helpers
- [x] Puppet packages.pp: java-1.8.0 →
  java-21, yum repos → dnf repos,
  RHEL 7 URLs → RHEL 10 URLs
- [x] Systemd unit: JAVA_HOME updated to
  java-21-openjdk, added ProtectSystem,
  ProtectHome, NoNewPrivileges, PrivateTmp
- [x] OpenSSL: removed 1.0.2k pin; RHEL 10
  uses 3.x by default
- [x] yum-plugin-versionlock →
  python3-dnf-plugin-versionlock
- [x] Tests passing (mvn test)
- [x] Build passing (mvn clean package)

---

## Requires Team Coordination

### RPM / Packaging Team

- [ ] **Recompile libpdf_ubs.so** for RHEL 10
  - Current: built for RHEL 7
    (glibc 2.17, OpenSSL 1.0.2k)
  - Target: glibc 2.39+, OpenSSL 3.x
  - **Binary-only** — no source available
  - RPM: `ubs-libpdf` from
    yum.internal.ubs.com
  - Without this, PDF parsing is disabled
    (graceful degradation in code)
  - Contact: RPM packaging team
- [ ] Update internal RPM repo URL from
  `/rhel7-custom/` to `/rhel10-custom/`
- [ ] Rebuild Oracle Instant Client RPM if
  custom build exists

### Network / Infrastructure Team

- [ ] Verify L3 connectivity from new RHEL 10
  host to:
  - `ora-auditdb-01.internal:1521`
    (Oracle audit DB)
  - `settle-cache-01.internal:6379`
    (Redis cache, no TLS)
  - `10.192.4.47:8080` (SOAP compliance
    endpoint)
  - `splunk-fwd.internal:514` (rsyslog)
- [ ] Update firewall rules on new host
  (nftables, not iptables)
- [ ] Verify no IP-based ACLs block the
  new host's address

### Storage Team

- [ ] Configure NFS mounts on new RHEL 10
  host:
  - `nas-docs-01.internal:/export/trade-docs`
    → `/mnt/shared-docs`
  - `nas-docs-01.internal`
    `:/export/compliance-feeds`
    → `/mnt/compliance-feeds`
  - `nas-archive-01.internal`
    `:/export/doc-archive`
    → `/mnt/doc-archive`
- [ ] Verify NFS v4 compatibility and mount
  options on RHEL 10

### DNS / Load Balancer Team

- [ ] Update DNS records if host IP changes
- [ ] Update load balancer pool membership
  (if applicable)

### Compliance Team

- [ ] Verify SOAP endpoint
  `http://10.192.4.47:8080`
  `/compliance-api/v1/validate`
  accepts connections from new host
- [ ] No migration planned for compliance
  service itself

---

## Smoke Testing & Cutover

- [ ] Deploy to RHEL 10 staging host
- [ ] Verify Java 21 startup:
  `java -version` shows 21
- [ ] Verify systemd service starts:
  `systemctl start doc-pipeline`
- [ ] Verify NFS mounts active:
  `mountpoint /mnt/shared-docs`
- [ ] Verify Oracle connectivity:
  `deploy/scripts/check-oracle.sh`
- [ ] Verify Redis connectivity:
  `redis-cli -h settle-cache-01.internal`
  `ping`
- [ ] Drop test PDF into
  `/opt/ubs/incoming-docs/` and verify
  end-to-end processing
- [ ] Verify Splunk log forwarding via
  rsyslog
- [ ] Run nightly reconciliation manually:
  `deploy/scripts/reconciliation.sh`
- [ ] Schedule maintenance window for
  production cutover
- [ ] Execute production cutover
- [ ] Monitor health for 48h post-cutover
- [ ] Decommission RHEL 7 host after soak
  period
