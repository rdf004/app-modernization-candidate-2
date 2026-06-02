# deploy/manifests/packages.pp
#
# Package installation from internal RPM
# repository. Installs Java 8, native PDF
# library, and required system packages.

class docpipeline::packages {

  # Java 8 — pinned to RHEL 7 OpenJDK
  package { 'java-1.8.0-openjdk':
    ensure => 'installed',
  }

  package { 'java-1.8.0-openjdk-devel':
    ensure => 'installed',
  }

  # libpdf_ubs.so — custom UBS PDF parser
  # Built for RHEL 7 x86_64 ONLY.
  # Source code not available.
  # RPM from internal repo:
  #   yum.internal.ubs.com/rhel7-custom/
  package { 'ubs-libpdf':
    ensure  => '2.3.1-1.el7',
    require => Yumrepo['ubs-internal'],
  }

  # NFS client utilities
  package { 'nfs-utils':
    ensure => 'installed',
  }

  # Redis CLI for health checks
  package { 'redis':
    ensure => 'installed',
  }

  # Oracle Instant Client for sqlplus
  package { 'oracle-instantclient-basic':
    ensure  => '21.1.0.0.0-1',
    require => Yumrepo['oracle-public'],
  }

  # Internal RPM repository
  yumrepo { 'ubs-internal':
    descr    => 'UBS Internal RHEL 7 RPMs',
    baseurl  =>
      'https://yum.internal.ubs.com'
      . '/rhel7-custom/',
    enabled  => 1,
    gpgcheck => 1,
    gpgkey   =>
      'https://yum.internal.ubs.com'
      . '/RPM-GPG-KEY-ubs',
  }

  yumrepo { 'oracle-public':
    descr    => 'Oracle Instant Client',
    baseurl  =>
      'https://yum.oracle.com'
      . '/repo/OracleLinux/OL7/latest/x86_64',
    enabled  => 1,
    gpgcheck => 1,
    gpgkey   =>
      'https://yum.oracle.com'
      . '/RPM-GPG-KEY-oracle-ol7',
  }
}
