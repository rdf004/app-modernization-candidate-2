# deploy/manifests/rsyslog.pp
#
# Rsyslog configuration for forwarding
# application logs to Splunk. OS-level
# syslog integration — not application
# managed.

class docpipeline::rsyslog {

  file { '/etc/rsyslog.d/60-docpipeline.conf':
    ensure  => 'file',
    content => template(
      'docpipeline/rsyslog.conf.erb'
    ),
    notify  => Service['rsyslog'],
  }

  service { 'rsyslog':
    ensure => 'running',
    enable => true,
  }

  # Logrotate config for app logs
  file {
    '/etc/logrotate.d/ubs-docpipeline':
    ensure  => 'file',
    content => '/var/log/ubs/*.log {
    daily
    rotate 30
    compress
    delaycompress
    missingok
    notifempty
    create 0644 docpipeline docpipeline
    postrotate
        /bin/systemctl reload rsyslog
    endscript
}
',
  }
}
