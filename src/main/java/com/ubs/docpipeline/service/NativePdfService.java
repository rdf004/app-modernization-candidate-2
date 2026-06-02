package com.ubs.docpipeline.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;

/**
 * JNI bridge to libpdf_ubs.so — a custom UBS
 * PDF parsing library installed at
 * /usr/local/lib/ via internal RPM. Compiled
 * specifically for RHEL 7 x86_64. NOT available
 * as a Maven dependency. Requires recompilation
 * for any OS migration.
 *
 * RPM: ubs-libpdf-2.3.1-1.el7.x86_64.rpm
 * Source: not available (binary-only distro)
 */
@Service
public class NativePdfService {

    private static final Logger LOG =
        LoggerFactory.getLogger(
            NativePdfService.class
        );

    private static final String LIB_PATH =
        "/usr/local/lib/libpdf_ubs.so";

    private boolean nativeAvailable = false;

    static {
        try {
            System.loadLibrary("pdf_ubs");
        } catch (UnsatisfiedLinkError e) {
            LoggerFactory.getLogger(
                NativePdfService.class
            ).warn(
                "libpdf_ubs.so not found. "
                + "PDF parsing disabled. "
                + "Ensure RPM is installed."
            );
        }
    }

    @PostConstruct
    public void init() {
        File lib = new File(LIB_PATH);
        nativeAvailable = lib.exists();
        if (nativeAvailable) {
            LOG.info(
                "libpdf_ubs.so loaded from {}",
                LIB_PATH
            );
        } else {
            LOG.error(
                "CRITICAL: {} not found. "
                + "Install ubs-libpdf RPM.",
                LIB_PATH
            );
        }
    }

    /**
     * Parse PDF and extract text content.
     * Delegates to native libpdf_ubs.so.
     */
    public native byte[] parsePdf(
        byte[] pdfData
    );

    /**
     * Extract metadata from PDF document.
     * Delegates to native libpdf_ubs.so.
     */
    public native String extractMetadata(
        byte[] pdfData
    );

    /**
     * Validate PDF structure and signatures.
     * Delegates to native libpdf_ubs.so.
     */
    public native boolean validatePdf(
        byte[] pdfData
    );

    public boolean isNativeAvailable() {
        return nativeAvailable;
    }
}
