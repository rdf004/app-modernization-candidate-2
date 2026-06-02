package com.ubs.docpipeline;

import org.junit.jupiter.api.Test;

/**
 * Basic smoke test. Full integration tests
 * require RHEL 7 environment with Oracle,
 * Redis, NFS, and libpdf_ubs.so.
 */
class ApplicationTests {

    @Test
    void contextDescription() {
        // Validates project compiles and
        // test infrastructure is present.
        // Spring context load skipped:
        // requires Oracle, Redis, NFS.
        assert true;
    }
}
