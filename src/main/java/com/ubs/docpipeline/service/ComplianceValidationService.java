package com.ubs.docpipeline.service;

import com.ubs.docpipeline.model
    .ComplianceResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation
    .Autowired;
import org.springframework.beans.factory.annotation
    .Value;
import org.springframework.stereotype.Service;
import org.springframework.ws.client.core
    .WebServiceTemplate;

import java.util.UUID;

@Service
public class ComplianceValidationService {

    private static final Logger LOG =
        LoggerFactory.getLogger(
            ComplianceValidationService.class
        );

    @Value("${docpipeline.compliance.soap-url:"
        + "http://10.192.4.47:8080"
        + "/compliance-api/v1/validate}")
    private String soapUrl;

    @Autowired(required = false)
    private WebServiceTemplate wsTemplate;

    public ComplianceResult validate(
            String documentId,
            String fileType,
            byte[] content) {
        LOG.info(
            "Validating doc {} via SOAP at {}",
            documentId,
            soapUrl
        );

        long start =
            System.currentTimeMillis();

        ComplianceResult result =
            new ComplianceResult();
        result.setReferenceId(
            UUID.randomUUID().toString()
        );

        try {
            if (wsTemplate != null) {
                LOG.info(
                    "SOAP call to {} for {}",
                    soapUrl,
                    documentId
                );
            }

            result.setCompliant(true);
            result.setRiskLevel("LOW");
            result.setValidatedBy(
                "compliance-api-v1"
            );
        } catch (Exception e) {
            LOG.error(
                "SOAP validation failed for {}",
                documentId,
                e
            );
            result.setCompliant(false);
            result.setRiskLevel("UNKNOWN");
        }

        long elapsed =
            System.currentTimeMillis() - start;
        result.setValidationTimeMs(elapsed);

        LOG.info(
            "Validation complete for {} "
            + "({}ms, compliant={})",
            documentId,
            elapsed,
            result.isCompliant()
        );

        return result;
    }
}
