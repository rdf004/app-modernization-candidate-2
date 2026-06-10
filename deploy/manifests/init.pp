# deploy/manifests/init.pp
#
# Puppet manifest: UBS Document Processing
# Pipeline — RHEL 10 deployment.
#
# Modernized from RHEL 7:
# - Java 21 (OpenJDK)
# - OpenSSL 3.x (system default)
# - dnf replaces yum
# - systemd 255+, nftables
# - libpdf_ubs.so requires RHEL 10 rebuild

class docpipeline (
  $app_version = '2.0.0-SNAPSHOT',
  $oracle_host =
    'ora-auditdb-01.internal',
  $redis_host =
    'settle-cache-01.internal',
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
