package com.ubs.docpipeline.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.time.Instant;

@Entity
@Table(name = "processed_documents")
public class ProcessedDocument {

    public enum Status {
        RECEIVED,
        PROCESSING,
        VALIDATED,
        COMPLIANT,
        NON_COMPLIANT,
        FAILED,
        ARCHIVED
    }

    @Id
    @GeneratedValue(
        strategy = GenerationType.IDENTITY
    )
    private Long id;

    @Column(
        name = "document_id",
        nullable = false,
        unique = true
    )
    private String documentId;

    @Column(
        name = "file_name",
        nullable = false
    )
    private String fileName;

    @Column(name = "file_type")
    private String fileType;

    @Column(name = "source_path")
    private String sourcePath;

    @Column(name = "output_path")
    private String outputPath;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Status status;

    @Column(
        name = "compliance_ref",
        length = 64
    )
    private String complianceRef;

    @Column(name = "checksum")
    private String checksum;

    @Column(name = "file_size_bytes")
    private long fileSizeBytes;

    @Column(name = "received_at")
    private Instant receivedAt;

    @Column(name = "processed_at")
    private Instant processedAt;

    @Column(
        name = "processing_host",
        length = 128
    )
    private String processingHost;

    public ProcessedDocument() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public void setSourcePath(String path) {
        this.sourcePath = path;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public void setOutputPath(String path) {
        this.outputPath = path;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getComplianceRef() {
        return complianceRef;
    }

    public void setComplianceRef(String ref) {
        this.complianceRef = ref;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public long getFileSizeBytes() {
        return fileSizeBytes;
    }

    public void setFileSizeBytes(long size) {
        this.fileSizeBytes = size;
    }

    public Instant getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(Instant at) {
        this.receivedAt = at;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(Instant at) {
        this.processedAt = at;
    }

    public String getProcessingHost() {
        return processingHost;
    }

    public void setProcessingHost(String host) {
        this.processingHost = host;
    }
}
