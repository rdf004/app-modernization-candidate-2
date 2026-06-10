# deploy/manifests/openssl.pp
#
# RHEL 10 ships OpenSSL 3.x by default.
# The 1.0.2k pin is removed — native
# library must be recompiled against
# OpenSSL 3.x as part of the migration.

class docpipeline::openssl {

  # Use system-default OpenSSL 3.x
  package { 'openssl':
    ensure => 'installed',
  }

  package { 'openssl-libs':
    ensure => 'installed',
  }

  # No version pin — RHEL 10 OpenSSL 3.x
  # is the supported target. The native
  # libpdf_ubs.so RPM (2.4.0+) must be
  # rebuilt against this version.
}
