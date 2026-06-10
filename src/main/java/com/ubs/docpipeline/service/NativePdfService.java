package com.ubs.docpipeline.service;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation
    .Value;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class NativePdfService {

    private static final Logger LOG =
        LoggerFactory.getLogger(
            NativePdfService.class
        );

    private final String libPath;
    private boolean nativeAvailable = false;

    public NativePdfService(
            @Value("${app.native.lib-path:"
                + "/usr/local/lib}")
            String libPath) {
        this.libPath = libPath;
    }

    static {
        try {
            System.loadLibrary("pdf_ubs");
        } catch (UnsatisfiedLinkError e) {
            LoggerFactory.getLogger(
                NativePdfService.class
            ).warn(
                "libpdf_ubs.so not found — "
                + "PDF parsing disabled"
            );
        }
    }

    @PostConstruct
    public void init() {
        File lib = new File(
            libPath + "/libpdf_ubs.so"
        );
        nativeAvailable = lib.exists();
        if (nativeAvailable) {
            LOG.info(
                "libpdf_ubs.so loaded from {}",
                libPath
            );
        } else {
            LOG.warn(
                "libpdf_ubs.so not at {} — "
                + "native PDF disabled",
                libPath
            );
        }
    }

    public native byte[] parsePdf(
        byte[] pdfData
    );

    public native String extractMetadata(
        byte[] pdfData
    );

    public native boolean validatePdf(
        byte[] pdfData
    );

    public boolean isNativeAvailable() {
        return nativeAvailable;
    }
}
