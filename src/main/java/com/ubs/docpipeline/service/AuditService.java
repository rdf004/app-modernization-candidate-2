package com.ubs.docpipeline.service;

import com.ubs.docpipeline.model.AuditRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;

@Service
public class AuditService {

    private static final Logger LOG =
        LoggerFactory.getLogger(
            AuditService.class
        );

    @PersistenceContext
    private EntityManager em;

    @Transactional
    public void logAction(
            String documentId,
            String action,
            String detail) {
        AuditRecord record = new AuditRecord();
        record.setDocumentId(documentId);
        record.setAction(action);
        record.setDetail(detail);
        record.setCreatedAt(new Date());
        record.setActor("doc-pipeline-svc");

        try {
            InetAddress local =
                InetAddress.getLocalHost();
            record.setHostname(
                local.getHostName()
            );
            record.setSourceIp(
                local.getHostAddress()
            );
        } catch (UnknownHostException e) {
            LOG.warn(
                "Cannot resolve local host", e
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
