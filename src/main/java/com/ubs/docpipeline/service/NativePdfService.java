package com.ubs.docpipeline.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation
    .Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;

@Service
public class NativePdfService {

    private static final Logger LOG =
        LoggerFactory.getLogger(
            NativePdfService.class
        );

    @Value("${docpipeline.native.lib-path:"
        + "/usr/local/lib}")
    private String libPath;

    @Value("${docpipeline.native.lib-name:"
        + "pdf_ubs}")
    private String libName;

    private boolean nativeAvailable = false;

    static {
        try {
            System.loadLibrary("pdf_ubs");
        } catch (UnsatisfiedLinkError e) {
            LoggerFactory.getLogger(
                NativePdfService.class
            ).warn(
                "libpdf_ubs.so not found."
                + " PDF parsing disabled."
                + " Ensure RPM is installed."
            );
        }
    }

    @PostConstruct
    public void init() {
        String fullPath = libPath
            + "/lib" + libName + ".so";
        File lib = new File(fullPath);
        nativeAvailable = lib.exists();
        if (nativeAvailable) {
            LOG.info(
                "Native lib loaded from {}",
                fullPath
            );
        } else {
            LOG.error(
                "CRITICAL: {} not found."
                + " Install RPM.",
                fullPath
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
