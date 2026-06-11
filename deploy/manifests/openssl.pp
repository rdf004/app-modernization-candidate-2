# deploy/manifests/openssl.pp
#
# OpenSSL on RHEL 10: version 3.x is the
# system default. No longer pinned.
#
# NOTE: libpdf_ubs.so was linked against
# OpenSSL 1.0.2 on RHEL 7 and MUST be
# recompiled by the RPM team before it
# will work with OpenSSL 3.x on RHEL 10.

class docpipeline::openssl {

  # RHEL 10 ships OpenSSL 3.x by default.
  # Ensure the system package is present.
  package { 'openssl':
    ensure => 'installed',
  }

  package { 'openssl-libs':
    ensure => 'installed',
  }

  # TODO [RPM Team]: After recompiling
  # ubs-libpdf for OpenSSL 3.x, remove
  # any legacy compat shims that may
  # have been installed as a workaround.

  # dnf versionlock plugin (RHEL 10)
  package {
    'python3-dnf-plugin-versionlock':
    ensure => 'installed',
  }
}
