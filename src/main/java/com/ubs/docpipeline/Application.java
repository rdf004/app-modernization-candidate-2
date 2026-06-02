package com.ubs.docpipeline;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure
    .SpringBootApplication;
import org.springframework.scheduling.annotation
    .EnableScheduling;

/**
 * UBS Document Processing Pipeline.
 *
 * Watches /opt/ubs/incoming-docs/ for PDF/CSV,
 * processes via Camel routes, validates against
 * internal SOAP compliance service, writes
 * results to /opt/ubs/processed/ and reports
 * to /opt/ubs/reports/.
 *
 * Requires RHEL 7 host with:
 * - libpdf_ubs.so in /usr/local/lib/
 * - NFS mount at /mnt/shared-docs/
 * - Oracle DB for audit logging
 * - Redis at settle-cache-01.internal:6379
 * - Network path to 10.192.4.47:8080
 */
@SpringBootApplication
@EnableScheduling
public class Application {

    public static void main(String[] args) {
        System.setProperty(
            "java.library.path",
            "/usr/local/lib"
        );
        SpringApplication.run(
            Application.class, args
        );
    }
}
