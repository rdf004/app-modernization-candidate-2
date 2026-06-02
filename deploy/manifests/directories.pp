# deploy/manifests/directories.pp
#
# Creates the directory structure required
# by the document processing pipeline.

class docpipeline::directories {

  $base_dirs = [
    '/opt/ubs',
    '/opt/ubs/incoming-docs',
    '/opt/ubs/incoming-docs/.done',
    '/opt/ubs/incoming-docs/.error',
    '/opt/ubs/processed',
    '/opt/ubs/reports',
    '/opt/ubs/reports/flagged',
    '/opt/ubs/locks',
    '/opt/ubs/app',
    '/opt/ubs/config',
    '/var/log/ubs',
  ]

  file { $base_dirs:
    ensure => 'directory',
    owner  => 'docpipeline',
    group  => 'docpipeline',
    mode   => '0755',
  }

  # NFS mount point
  file { '/mnt/shared-docs':
    ensure => 'directory',
    owner  => 'root',
    group  => 'root',
    mode   => '0755',
  }

  file { '/mnt/shared-docs/incoming':
    ensure  => 'directory',
    owner   => 'docpipeline',
    group   => 'docpipeline',
    mode    => '0755',
    require => File['/mnt/shared-docs'],
  }

  # Service account
  user { 'docpipeline':
    ensure     => 'present',
    shell      => '/bin/bash',
    home       => '/opt/ubs',
    managehome => false,
    system     => true,
  }

  group { 'docpipeline':
    ensure => 'present',
    system => true,
  }
}
