# deploy/manifests/service.pp
#
# Systemd service definition and app
# deployment for RHEL 10.

class docpipeline::service (
  $app_version =
    $docpipeline::app_version,
) {

  # Deploy JAR from artifact repository
  file { '/opt/ubs/app/doc-processing'
    . '-pipeline.jar':
    ensure => 'file',
    source =>
      'puppet:///modules/docpipeline/'
      . "doc-processing-pipeline-"
      . "${app_version}.jar",
    owner  => 'docpipeline',
    group  => 'docpipeline',
    mode   => '0644',
    notify => Service['doc-pipeline'],
  }

  # Systemd unit file (RHEL 10 / systemd 255+)
  file { '/etc/systemd/system/'
    . 'doc-pipeline.service':
    ensure  => 'file',
    content => '[Unit]
Description=UBS Doc Processing Pipeline
After=network.target
Requires=network.target

[Service]
Type=simple
User=docpipeline
Group=docpipeline
WorkingDirectory=/opt/ubs
ExecStart=/usr/bin/java \
  -Xms512m -Xmx2048m \
  -Djava.library.path=/usr/local/lib \
  -Dspring.profiles.active=production \
  -jar /opt/ubs/app/\
doc-processing-pipeline.jar
Restart=on-failure
RestartSec=10
StandardOutput=journal
StandardError=journal
SyslogIdentifier=doc-pipeline
LimitNOFILE=65536
Environment=JAVA_HOME=\
/usr/lib/jvm/java-21-openjdk
Environment=LD_LIBRARY_PATH=\
/usr/local/lib

# RHEL 10 systemd hardening
ProtectSystem=strict
ProtectHome=yes
NoNewPrivileges=yes
PrivateTmp=yes
ReadWritePaths=/opt/ubs
ReadWritePaths=/var/log/ubs
ReadWritePaths=/mnt/shared-docs
ReadWritePaths=/mnt/doc-archive

[Install]
WantedBy=multi-user.target
',
    notify  => Exec['systemd-reload'],
  }

  exec { 'systemd-reload':
    command     =>
      '/bin/systemctl daemon-reload',
    refreshonly => true,
  }

  service { 'doc-pipeline':
    ensure  => 'running',
    enable  => true,
    require => [
      File['/opt/ubs/app/doc-processing'
        . '-pipeline.jar'],
      File['/etc/systemd/system/'
        . 'doc-pipeline.service'],
      Package['java-21-openjdk'],
      Package['ubs-libpdf'],
    ],
  }
}
