# deploy/manifests/openssl.pp
#
# Pin OpenSSL to 1.0.2k on RHEL 7.
# This version is EOL but required by
# libpdf_ubs.so and the Oracle Instant
# Client. Upgrading OpenSSL without
# recompiling native dependencies will
# break the application.

class docpipeline::openssl {

  package { 'openssl':
    ensure => '1.0.2k-26.el7_9',
  }

  package { 'openssl-libs':
    ensure => '1.0.2k-26.el7_9',
  }

  package { 'openssl-devel':
    ensure => '1.0.2k-26.el7_9',
  }

  # Prevent yum from upgrading OpenSSL
  # during routine patching.
  # libpdf_ubs.so is linked against 1.0.2
  # and will segfault on 1.1.x+
  yumrepo { 'ubs-openssl-pin':
    descr   => 'OpenSSL version pin',
    baseurl =>
      'https://yum.internal.ubs.com'
      . '/rhel7-pinned/',
    enabled => 1,
    exclude => 'openssl*',
  }

  file {
    '/etc/yum/pluginconf.d/versionlock.list':
    ensure  => 'file',
    content => '0:openssl-1.0.2k-26.el7_9.*
0:openssl-libs-1.0.2k-26.el7_9.*
0:openssl-devel-1.0.2k-26.el7_9.*
',
  }

  package { 'yum-plugin-versionlock':
    ensure => 'installed',
  }
}
