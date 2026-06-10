# deploy/manifests/packages.pp
#
# RHEL 10 packages via dnf. Java 21,
# native PDF library (RHEL 10 rebuild),
# and required system packages.

class docpipeline::packages {

  # Java 21 — RHEL 10 OpenJDK
  package { 'java-21-openjdk':
    ensure => 'installed',
  }

  package { 'java-21-openjdk-devel':
    ensure => 'installed',
  }

  # libpdf_ubs.so — rebuilt for RHEL 10
  # Requires recompilation against
  # OpenSSL 3.x and glibc 2.39+
  package { 'ubs-libpdf':
    ensure  => '2.4.0-1.el10',
    require =>
      Yumrepo['ubs-internal'],
  }

  # NFS client utilities
  package { 'nfs-utils':
    ensure => 'installed',
  }

  # Redis CLI for health checks
  package { 'redis':
    ensure => 'installed',
  }

  # Oracle Instant Client 23c
  package {
    'oracle-instantclient-basic':
    ensure  => '23.6.0.0.0-1',
    require =>
      Yumrepo['oracle-public'],
  }

  # Internal RPM repository (RHEL 10)
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
      'https://yum.oracle.com/repo/'
      . 'OracleLinux/OL10/'
      . 'latest/x86_64',
    enabled  => 1,
    gpgcheck => 1,
    gpgkey   =>
      'https://yum.oracle.com'
      . '/RPM-GPG-KEY-oracle-ol10',
  }
}
