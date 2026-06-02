# deploy/manifests/init.pp
#
# Puppet manifest: UBS Document Processing
# Pipeline — RHEL 7 deployment orchestration.
#
# Manages:
# - libpdf_ubs.so from internal RPM repo
# - Directory structure under /opt/ubs/
# - NFS mounts from upstream systems
# - Crontab entries for batch reconciliation
# - Rsyslog forwarding to Splunk
# - OpenSSL 1.0.2 pinning
# - Java 8 and application deployment

class docpipeline (
  $app_version = '1.0.0-SNAPSHOT',
  $oracle_host = 'ora-auditdb-01.internal',
  $redis_host  = 'settle-cache-01.internal',
  $splunk_host = 'splunk-fwd.internal',
  $nfs_server  = 'nas-docs-01.internal',
) {
  include docpipeline::packages
  include docpipeline::directories
  include docpipeline::nfs_mounts
  include docpipeline::crontab
  include docpipeline::rsyslog
  include docpipeline::openssl
  include docpipeline::service
}
