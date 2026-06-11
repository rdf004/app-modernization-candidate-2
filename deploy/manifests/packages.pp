# deploy/manifests/packages.pp
#
# Package installation via dnf (RHEL 10).
# Installs Java 21, native PDF library,
# and required system packages.

class docpipeline::packages {

  # Java 21 — RHEL 10 LTS OpenJDK
  package { 'java-21-openjdk':
    ensure => 'installed',
  }

  package { 'java-21-openjdk-devel':
    ensure => 'installed',
  }

  # TODO [RPM Team]: Recompile ubs-libpdf
  # for RHEL 10 (glibc 2.39+, OpenSSL 3.x).
  # Current: built for RHEL 7 (glibc 2.17).
  # Binary-only; no source available to
  # Devin. Contact RPM packaging team.
  package { 'ubs-libpdf':
    ensure  => 'installed',
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
    ensure  => 'installed',
    require => Yumrepo['oracle-public'],
  }

  # Internal RPM repository (dnf)
  yumrepo { 'ubs-internal':
    descr    =>
      'UBS Internal RHEL 10 RPMs',
    baseurl  =>
      'https://yum.internal.ubs.com'
      . '/rhel10-custom/',
    enabled  => 1,
    gpgcheck => 1,
    gpgkey   =>
      'https://yum.internal.ubs.com'
      . '/RPM-GPG-KEY-ubs',
  }

  yumrepo { 'oracle-public':
    descr    => 'Oracle Instant Client',
    baseurl  =>
      'https://yum.oracle.com/repo'
      . '/OracleLinux/OL10'
      . '/latest/x86_64',
    enabled  => 1,
    gpgcheck => 1,
    gpgkey   =>
      'https://yum.oracle.com'
      . '/RPM-GPG-KEY-oracle-ol10',
  }
}
