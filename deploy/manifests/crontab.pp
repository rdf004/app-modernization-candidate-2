# deploy/manifests/crontab.pp
#
# OS-level crontab entries — RHEL 10.
# Paths and hosts parameterized via Puppet
# variables instead of hardcoded values.

class docpipeline::crontab (
  $redis_host =
    $docpipeline::redis_host,
  $compliance_url =
    'http://10.192.4.47:8080',
) {

  # Nightly batch reconciliation — 2 AM
  cron { 'doc-reconciliation':
    command =>
      '/opt/ubs/scripts/'
      . 'reconciliation.sh '
      . '>> /var/log/ubs/reconcile.log '
      . '2>&1',
    user    => 'docpipeline',
    hour    => 2,
    minute  => 0,
  }

  # Hourly lock file cleanup
  cron { 'lock-cleanup':
    command =>
      'find /opt/ubs/locks '
      . '-name "*.lock" -mmin +60 '
      . '-delete',
    user    => 'docpipeline',
    hour    => '*',
    minute  => 15,
  }

  # Archive processed docs daily at 3 AM
  cron { 'archive-processed':
    command =>
      'find /opt/ubs/processed '
      . '-mtime +30 -exec mv {} '
      . '/mnt/doc-archive/ \;',
    user    => 'docpipeline',
    hour    => 3,
    minute  => 0,
  }

  # NFS health check every 5 minutes
  cron { 'nfs-health-check':
    command =>
      'stat /mnt/shared-docs '
      . '> /dev/null 2>&1 '
      . '|| logger -p local0.err '
      . '"NFS mount unavailable"',
    user    => 'root',
    minute  => '*/5',
  }

  # Redis connectivity check
  cron { 'redis-health':
    command =>
      "redis-cli -h ${redis_host} "
      . 'ping > /dev/null 2>&1 '
      . '|| logger -p local0.err '
      . '"Redis unreachable"',
    user    => 'docpipeline',
    minute  => '*/10',
  }

  # Oracle audit DB connectivity check
  cron { 'oracle-health':
    command =>
      '/opt/ubs/scripts/'
      . 'check-oracle.sh '
      . '>> /var/log/ubs/'
      . 'oracle-check.log 2>&1',
    user    => 'docpipeline',
    minute  => '*/15',
  }

  # Disk space monitoring
  cron { 'disk-space-alert':
    command =>
      'df /opt/ubs '
      . '| awk \'NR==2{if($5+0>85)'
      . ' print "WARN: disk " $5}\' '
      . '| logger -p local0.warning',
    user    => 'root',
    minute  => '*/30',
  }

  # SOAP endpoint health check
  cron { 'soap-health':
    command =>
      "curl -sf ${compliance_url}"
      . '/compliance-api/v1/health '
      . '> /dev/null 2>&1 '
      . '|| logger -p local0.err '
      . '"SOAP endpoint down"',
    user    => 'docpipeline',
    minute  => '*/5',
  }

  # Log rotation trigger
  cron { 'log-rotate-ubs':
    command =>
      '/usr/sbin/logrotate '
      . '/etc/logrotate.d/'
      . 'ubs-docpipeline',
    user    => 'root',
    hour    => 4,
    minute  => 0,
  }

  # Splunk forwarder restart (weekly)
  cron { 'splunk-fwd-restart':
    command =>
      'systemctl restart '
      . 'splunkforwarder',
    user    => 'root',
    weekday => 0,
    hour    => 5,
    minute  => 0,
  }

  # Stale file detector
  cron { 'stale-incoming-alert':
    command =>
      'find /opt/ubs/incoming-docs '
      . '-name "*.pdf" -o '
      . '-name "*.csv" '
      . '| head -1 '
      . '| xargs -I{} stat -c %Y {} '
      . '2>/dev/null',
    user    => 'docpipeline',
    minute  => '*/20',
  }

  # NFS mount re-mount on failure
  cron { 'nfs-remount':
    command =>
      'mountpoint -q '
      . '/mnt/shared-docs '
      . '|| mount /mnt/shared-docs',
    user    => 'root',
    minute  => '*/10',
  }
}
