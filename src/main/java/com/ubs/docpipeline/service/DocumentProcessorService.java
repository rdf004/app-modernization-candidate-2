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
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class DocumentProcessorService {

    private static final Logger LOG =
        LoggerFactory.getLogger(
            DocumentProcessorService.class
        );

    @Value("${docpipeline.dirs.processed:"
        + "/opt/ubs/processed}")
    private String processedDir;

    @Value("${docpipeline.dirs.reports:"
        + "/opt/ubs/reports}")
    private String reportsDir;

    @Value("${docpipeline.cache.prefix:"
        + "docpipeline:state:}")
    private String cachePrefix;

    @Value("${docpipeline.cache.ttl-hours:24}")
    private long cacheTtlHours;

    @Autowired
    private FileLockService lockSvc;

    @Autowired
    private NativePdfService pdfSvc;

    @Autowired
    private ComplianceValidationService
        complianceSvc;

    @Autowired
    private AuditService auditSvc;

    @Autowired(required = false)
    private RedisTemplate<String, Object>
        redisTemplate;

    public ProcessedDocument process(
            File incoming) throws IOException {
        String docId = UUID.randomUUID()
            .toString()
            .substring(0, 12);
        String fileName = incoming.getName();

        LOG.info(
            "Processing {} as {}",
            fileName,
            docId
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
                new ProcessedDocument();
            doc.setDocumentId(docId);
            doc.setFileName(fileName);
            doc.setSourcePath(
                incoming.getAbsolutePath()
            );
            doc.setReceivedAt(new Date());
            doc.setStatus(
                ProcessedDocument
                    .Status.PROCESSING
            );
            doc.setFileSizeBytes(
                incoming.length()
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
                docId,
                "RECEIVED",
                "File: " + fileName
                + " (" + data.length
                + " bytes)"
            );

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

            Path outPath = writeOutput(
                doc, data
            );
            doc.setOutputPath(
                outPath.toString()
            );
            doc.setProcessedAt(new Date());

            cacheState(
                docId,
                doc.getStatus().name()
            );

            auditSvc.logAction(
                docId,
                "PROCESSED",
                "Status: " + doc.getStatus()
            );

            LOG.info(
                "Completed {} — {}",
                docId,
                doc.getStatus()
            );

            return doc;
        } finally {
            lockSvc.releaseLock(lock, docId);
        }
    }

    private void processPdf(
            ProcessedDocument doc,
            byte[] data) {
        if (pdfSvc.isNativeAvailable()) {
            try {
                pdfSvc.validatePdf(data);
                String meta =
                    pdfSvc.extractMetadata(
                        data
                    );
                LOG.info(
                    "PDF metadata for {}: {}",
                    doc.getDocumentId(),
                    meta
                );
            } catch (Exception e) {
                LOG.error(
                    "Native PDF parsing"
                    + " failed for {}",
                    doc.getDocumentId(),
                    e
                );
            }
        } else {
            LOG.warn(
                "libpdf_ubs.so not loaded;"
                + " skipping native PDF parse"
            );
        }
    }

    private Path writeOutput(
            ProcessedDocument doc,
            byte[] data) throws IOException {
        Path procPath =
            Paths.get(processedDir);
        if (!Files.exists(procPath)) {
            Files.createDirectories(procPath);
        }

        String outName =
            doc.getDocumentId()
            + "_" + doc.getFileName();
        Path outPath =
            procPath.resolve(outName);
        Files.write(outPath, data);

        Path rptDir = Paths.get(reportsDir);
        if (!Files.exists(rptDir)) {
            Files.createDirectories(rptDir);
        }

        String rptName =
            doc.getDocumentId() + ".report";
        Path rptPath =
            rptDir.resolve(rptName);
        String rpt = String.format(
            "Document: %s%n"
            + "Status: %s%n"
            + "Compliance: %s%n"
            + "Checksum: %s%n"
            + "Size: %d bytes%n",
            doc.getFileName(),
            doc.getStatus(),
            doc.getComplianceRef(),
            doc.getChecksum(),
            doc.getFileSizeBytes()
        );
        Files.write(
            rptPath,
            rpt.getBytes()
        );

        return outPath;
    }

    private void cacheState(
            String docId, String state) {
        if (redisTemplate != null) {
            try {
                redisTemplate
                    .opsForValue()
                    .set(
                        cachePrefix + docId,
                        state,
                        cacheTtlHours,
                        TimeUnit.HOURS
                    );
            } catch (Exception e) {
                LOG.warn(
                    "Redis cache update"
                    + " failed for {}",
                    docId,
                    e
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
            StringBuilder sb =
                new StringBuilder();
            for (byte b : hash) {
                sb.append(
                    String.format("%02x", b)
                );
            }
            return sb.toString();
        } catch (Exception e) {
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
