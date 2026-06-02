package com.ubs.docpipeline.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Result from the internal SOAP compliance
 * validation service at 10.192.4.47:8080.
 */
public class ComplianceResult
        implements Serializable {

    private static final long serialVersionUID =
        1L;

    private String referenceId;
    private boolean compliant;
    private String riskLevel;
    private List<String> violations;
    private String validatedBy;
    private long validationTimeMs;

    public ComplianceResult() {
        this.violations = new ArrayList<>();
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String refId) {
        this.referenceId = refId;
    }

    public boolean isCompliant() {
        return compliant;
    }

    public void setCompliant(boolean compliant) {
        this.compliant = compliant;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public List<String> getViolations() {
        return violations;
    }

    public void setViolations(
            List<String> violations) {
        this.violations = violations;
    }

    public String getValidatedBy() {
        return validatedBy;
    }

    public void setValidatedBy(String validator) {
        this.validatedBy = validator;
    }

    public long getValidationTimeMs() {
        return validationTimeMs;
    }

    public void setValidationTimeMs(long ms) {
        this.validationTimeMs = ms;
    }
}
