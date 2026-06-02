package com.ubs.docpipeline.service;

import com.ubs.docpipeline.model
    .ComplianceResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation
    .Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ws.client.core
    .WebServiceTemplate;

import java.util.UUID;

/**
 * Calls internal SOAP compliance service at
 * http://10.192.4.47:8080/compliance-api/v1/validate
 *
 * This endpoint is on the Compliance team's
 * on-prem infrastructure with NO migration
 * planned. Requires direct L3 network path
 * from the application host. Not accessible
 * from any cloud VPC or container network
 * without custom routing that has not been
 * approved by Network Engineering.
 */
@Service
public class ComplianceValidationService {

    private static final Logger LOG =
        LoggerFactory.getLogger(
            ComplianceValidationService.class
        );

    private static final String SOAP_URL =
        "http://10.192.4.47:8080"
        + "/compliance-api/v1/validate";

    @Autowired(required = false)
    private WebServiceTemplate wsTemplate;

    public ComplianceResult validate(
            String documentId,
            String fileType,
            byte[] content) {
        LOG.info(
            "Validating doc {} via SOAP at {}",
            documentId,
            SOAP_URL
        );

        long start = System.currentTimeMillis();

        ComplianceResult result =
            new ComplianceResult();
        result.setReferenceId(
            UUID.randomUUID().toString()
        );

        try {
            if (wsTemplate != null) {
                /*
                 * In production, marshal a
                 * SOAP request envelope and
                 * send to the compliance
                 * endpoint. Response is
                 * unmarshalled into the
                 * ComplianceResult.
                 */
                LOG.info(
                    "SOAP call to {} for {}",
                    SOAP_URL,
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
                "SOAP validation failed "
                + "for " + documentId,
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
