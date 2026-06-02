# deploy/manifests/nfs_mounts.pp
#
# NFS mount configuration for receiving
# files from upstream banking systems.
# Three NFS mounts from different upstream
# document sources.

class docpipeline::nfs_mounts {

  # Primary document feed from Trade Ops
  mount { '/mnt/shared-docs':
    ensure  => 'mounted',
    device  =>
      'nas-docs-01.internal'
      . ':/export/trade-docs',
    fstype  => 'nfs',
    options =>
      'rw,hard,intr,rsize=8192,wsize=8192',
    atboot  => true,
    require => [
      Package['nfs-utils'],
      File['/mnt/shared-docs'],
    ],
  }

  # Compliance reports from regulatory team
  mount { '/mnt/compliance-feeds':
    ensure  => 'mounted',
    device  =>
      'nas-docs-01.internal'
      . ':/export/compliance-feeds',
    fstype  => 'nfs',
    options =>
      'ro,hard,intr,rsize=8192',
    atboot  => true,
    require => Package['nfs-utils'],
  }

  # Archive mount for long-term storage
  mount { '/mnt/doc-archive':
    ensure  => 'mounted',
    device  =>
      'nas-archive-01.internal'
      . ':/export/doc-archive',
    fstype  => 'nfs',
    options =>
      'rw,hard,intr,rsize=8192,wsize=8192',
    atboot  => true,
    require => Package['nfs-utils'],
  }

  file { '/mnt/compliance-feeds':
    ensure => 'directory',
    before => Mount['/mnt/compliance-feeds'],
  }

  file { '/mnt/doc-archive':
    ensure => 'directory',
    before => Mount['/mnt/doc-archive'],
  }
}
