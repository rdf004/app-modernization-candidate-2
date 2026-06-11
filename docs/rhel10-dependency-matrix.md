# RHEL 10 Dependency Compatibility Matrix

## Application: UBS Document Processing Pipeline

Host: `doc-proc-01.internal.ubs.com`
Source OS: RHEL 7.9 (Kernel 3.10.0)
Target OS: RHEL 10 (Kernel 6.x, glibc 2.39+)

## Runtime & Framework

| Dependency | Current (RHEL 7) | RHEL 10 Status | Target | Action | Owner |
|---|---|---|---|---|---|
| OpenJDK | 1.8.0.392 | ⬆️ Upgrade | java-21-openjdk | Upgrade runtime + source | Devin |
| Spring Boot | 2.7.18 | ⬆️ Upgrade | 3.4.x | Upgrade framework | Devin |
| Apache Camel | 3.14.10 | ⬆️ Upgrade | 4.8.x | Upgrade framework | Devin |
| Maven | 3.x | ✅ Available | 3.9.x | No change | — |

## System Packages

| Dependency | Current (RHEL 7) | RHEL 10 Status | Target | Action | Owner |
|---|---|---|---|---|---|
| OpenSSL | 1.0.2k-26.el7 | ⬆️ Upgrade | openssl 3.x | Upgrade; after libpdf rebuild | RPM Team |
| glibc | 2.17 | ⬆️ Upgrade | 2.39+ | Automatic with OS | — |
| GCC | 4.8 | ⬆️ Upgrade | 14.x | Automatic with OS | — |
| nfs-utils | 1.3.0 | ✅ Available | nfs-utils (latest) | No change | — |
| redis (CLI) | 4.0.14 | ⬆️ Upgrade | redis 7.x | Install from AppStream | Platform |
| Oracle Instant Client | 21.1 | ✅ Available | 21.x or 23.x | Update repo URL | Platform |
| cronie | RHEL 7 default | ✅ Available | cronie | No change | — |
| rsyslog | RHEL 7 default | ✅ Available | rsyslog (journald primary) | Minor config update | Devin |
| systemd | 219 | ⬆️ Upgrade | 255+ | Update unit files | Devin |
| curl | RHEL 7 default | ✅ Available | curl (latest) | No change | — |

## Native Libraries

| Dependency | Current (RHEL 7) | RHEL 10 Status | Target | Action | Owner |
|---|---|---|---|---|---|
| libpdf_ubs.so | 2.3.1-1.el7 | 🔧 Recompile | Rebuild for glibc 2.39+ / OpenSSL 3.x | **DO NOT attempt** | RPM Team |

## Package Manager

| Tool | Current (RHEL 7) | RHEL 10 Status | Target | Action | Owner |
|---|---|---|---|---|---|
| yum | yum 3.4.x | ❌ Removed | dnf | Replace all yum refs with dnf | Devin |
| yum-plugin-versionlock | RHEL 7 | 🔄 Renamed | dnf-plugin-versionlock (python3-dnf-plugin-versionlock) | Update Puppet | Devin |

## Networking

| Component | Current (RHEL 7) | RHEL 10 Status | Target | Action | Owner |
|---|---|---|---|---|---|
| iptables | iptables | ❌ Removed | nftables (nft CLI) | Replace if used | Devin |
| network-scripts | ifcfg scripts | ❌ Removed | NetworkManager (nmcli) | No impact (not used) | — |
| firewalld | firewalld | ✅ Available | firewalld (nftables backend) | Minor syntax check | Devin |

## Java-Specific Changes (8 → 21)

| Area | Change | Action |
|---|---|---|
| Namespace | `javax.*` → `jakarta.*` | Update all imports | Devin |
| JPA | `javax.persistence` → `jakarta.persistence` | Update entity annotations |
| Transactions | `javax.transaction` → `jakarta.transaction` | Update AuditService |
| Annotation | `javax.annotation` → `jakarta.annotation` | Update PostConstruct |
| JAXB/JAX-WS | Removed from JDK | Use standalone dependencies |
| Strong encapsulation | No `--illegal-access` | Remove if present |

## Key Risks

| Risk | Severity | Owner | Notes |
|---|---|---|---|
| libpdf_ubs.so recompile fails | **High** | RPM Team | Binary-only; no source code |
| SOAP endpoint network path | **High** | Network Eng | 10.192.4.47:8080 requires L3 path |
| OpenSSL 3.x breaks native deps | **Medium** | Platform | Must recompile libpdf first |
| NFS mount perf on new host | **Low** | Storage | Verify mount options |
| Redis version mismatch | **Low** | Platform | Redis 7.x is backward-compatible |
