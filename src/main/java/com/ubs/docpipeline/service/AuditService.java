package com.ubs.docpipeline.service;

import com.ubs.docpipeline.model.AuditRecord;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation
    .Value;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;

@Service
public class AuditService {

    private static final Logger LOG =
        LoggerFactory.getLogger(
            AuditService.class
        );

    @PersistenceContext
    private EntityManager em;

    @Value("${app.service-name:"
        + "doc-pipeline-svc}")
    private String serviceName;

    @Transactional
    public void logAction(
            String documentId,
            String action,
            String detail) {
        AuditRecord record = new AuditRecord();
        record.setDocumentId(documentId);
        record.setAction(action);
        record.setDetail(detail);
        record.setCreatedAt(Instant.now());
        record.setActor(serviceName);

        try {
            InetAddress addr =
                InetAddress.getLocalHost();
            record.setHostname(
                addr.getHostName()
            );
            record.setSourceIp(
                addr.getHostAddress()
            );
        } catch (UnknownHostException e) {
            LOG.warn(
                "Cannot resolve hostname",
                e
            );
            record.setHostname("unknown");
        }

        em.persist(record);

        LOG.info(
            "Audit: {} {} — {}",
            action,
            documentId,
            detail
        );
    }
}
