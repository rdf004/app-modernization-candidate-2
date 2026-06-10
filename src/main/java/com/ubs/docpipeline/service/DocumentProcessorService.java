package com.ubs.docpipeline.service;

import com.ubs.docpipeline.model
    .ComplianceResult;
import com.ubs.docpipeline.model
    .ProcessedDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation
    .Autowired;
import org.springframework.beans.factory.annotation
    .Value;
import org.springframework.data.redis.core
    .RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class DocumentProcessorService {

    private static final Logger LOG =
        LoggerFactory.getLogger(
            DocumentProcessorService.class
        );

    private static final String CACHE_PREFIX =
        "docpipeline:state:";
    private static final long
        CACHE_TTL_HOURS = 24;

    private final FileLockService lockSvc;
    private final NativePdfService pdfSvc;
    private final ComplianceValidationService
        complianceSvc;
    private final AuditService auditSvc;
    private final String processedDir;
    private final String reportsDir;

    @Autowired(required = false)
    private RedisTemplate<String, Object>
        redisTemplate;

    public DocumentProcessorService(
            FileLockService lockSvc,
            NativePdfService pdfSvc,
            ComplianceValidationService
                complianceSvc,
            AuditService auditSvc,
            @Value("${app.dirs.processed:"
                + "/opt/ubs/processed}")
            String processedDir,
            @Value("${app.dirs.reports:"
                + "/opt/ubs/reports}")
            String reportsDir) {
        this.lockSvc = lockSvc;
        this.pdfSvc = pdfSvc;
        this.complianceSvc = complianceSvc;
        this.auditSvc = auditSvc;
        this.processedDir = processedDir;
        this.reportsDir = reportsDir;
    }

    public ProcessedDocument process(
            File incoming) throws IOException {
        String docId = UUID.randomUUID()
            .toString().substring(0, 12);
        String fileName = incoming.getName();

        LOG.info(
            "Processing {} as {}",
            fileName, docId
        );

        FileLock lock =
            lockSvc.acquireLock(docId);
        if (lock == null) {
            LOG.warn(
                "Skipping {} — locked",
                fileName
            );
            return null;
        }

        try {
            ProcessedDocument doc =
                buildInitialDoc(
                    docId, fileName, incoming
                );
            cacheState(docId, "PROCESSING");

            byte[] data = Files.readAllBytes(
                incoming.toPath()
            );
            doc.setChecksum(
                computeChecksum(data)
            );

            String ext =
                getExtension(fileName);
            doc.setFileType(ext);

            if ("pdf".equalsIgnoreCase(ext)) {
                processPdf(doc, data);
            }

            auditSvc.logAction(
                docId, "RECEIVED",
                "File: " + fileName
                + " (" + data.length
                + " bytes)"
            );

            applyComplianceResult(
                doc, docId, ext, data
            );

            Path outPath =
                writeOutput(doc, data);
            doc.setOutputPath(
                outPath.toString()
            );
            doc.setProcessedAt(Instant.now());

            cacheState(
                docId,
                doc.getStatus().name()
            );

            auditSvc.logAction(
                docId, "PROCESSED",
                "Status: " + doc.getStatus()
            );

            LOG.info(
                "Completed {} — {}",
                docId, doc.getStatus()
            );
            return doc;
        } finally {
            lockSvc.releaseLock(lock, docId);
        }
    }

    private ProcessedDocument buildInitialDoc(
            String docId,
            String fileName,
            File incoming) {
        ProcessedDocument doc =
            new ProcessedDocument();
        doc.setDocumentId(docId);
        doc.setFileName(fileName);
        doc.setSourcePath(
            incoming.getAbsolutePath()
        );
        doc.setReceivedAt(Instant.now());
        doc.setStatus(
            ProcessedDocument.Status.PROCESSING
        );
        doc.setFileSizeBytes(incoming.length());
        return doc;
    }

    private void applyComplianceResult(
            ProcessedDocument doc,
            String docId,
            String ext,
            byte[] data) {
        ComplianceResult cr =
            complianceSvc.validate(
                docId, ext, data
            );
        doc.setComplianceRef(
            cr.getReferenceId()
        );
        if (cr.isCompliant()) {
            doc.setStatus(
                ProcessedDocument
                    .Status.COMPLIANT
            );
        } else {
            doc.setStatus(
                ProcessedDocument
                    .Status.NON_COMPLIANT
            );
        }
    }

    private void processPdf(
            ProcessedDocument doc,
            byte[] data) {
        if (pdfSvc.isNativeAvailable()) {
            try {
                pdfSvc.validatePdf(data);
                String meta =
                    pdfSvc
                        .extractMetadata(data);
                LOG.info(
                    "PDF metadata for {}: {}",
                    doc.getDocumentId(),
                    meta
                );
            } catch (
                UnsatisfiedLinkError e
            ) {
                LOG.error(
                    "Native PDF failed for {}",
                    doc.getDocumentId(),
                    e
                );
            }
        } else {
            LOG.warn(
                "libpdf_ubs.so not loaded; "
                + "skipping native PDF parse"
            );
        }
    }

    private Path writeOutput(
            ProcessedDocument doc,
            byte[] data) throws IOException {
        Path pDir = Paths.get(processedDir);
        if (!Files.exists(pDir)) {
            Files.createDirectories(pDir);
        }
        String outName =
            doc.getDocumentId()
            + "_" + doc.getFileName();
        Path outPath = pDir.resolve(outName);
        Files.write(outPath, data);

        Path rDir = Paths.get(reportsDir);
        if (!Files.exists(rDir)) {
            Files.createDirectories(rDir);
        }
        String rptName =
            doc.getDocumentId() + ".report";
        Path rptPath = rDir.resolve(rptName);
        String rpt = """
            Document: %s
            Status: %s
            Compliance: %s
            Checksum: %s
            Size: %d bytes
            """.formatted(
                doc.getFileName(),
                doc.getStatus(),
                doc.getComplianceRef(),
                doc.getChecksum(),
                doc.getFileSizeBytes()
            );
        Files.writeString(rptPath, rpt);
        return outPath;
    }

    private void cacheState(
            String docId, String state) {
        if (redisTemplate != null) {
            try {
                redisTemplate
                    .opsForValue()
                    .set(
                        CACHE_PREFIX + docId,
                        state,
                        CACHE_TTL_HOURS,
                        TimeUnit.HOURS
                    );
            } catch (RuntimeException e) {
                LOG.warn(
                    "Redis cache failed "
                    + "for {}",
                    docId, e
                );
            }
        }
    }

    private String computeChecksum(
            byte[] data) {
        try {
            MessageDigest md =
                MessageDigest.getInstance(
                    "SHA-256"
                );
            byte[] hash = md.digest(data);
            return HexFormat.of()
                .formatHex(hash);
        } catch (
            NoSuchAlgorithmException e
        ) {
            LOG.error(
                "SHA-256 unavailable", e
            );
            return "CHECKSUM_ERROR";
        }
    }

    private String getExtension(String name) {
        int dot = name.lastIndexOf('.');
        if (dot > 0) {
            return name.substring(dot + 1);
        }
        return "unknown";
    }
}
