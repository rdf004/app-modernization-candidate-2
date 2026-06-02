package com.ubs.docpipeline.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation
    .Configuration;
import org.springframework.oxm.jaxb
    .Jaxb2Marshaller;
import org.springframework.ws.client.core
    .WebServiceTemplate;
import org.springframework.ws.transport.http
    .HttpComponentsMessageSender;

/**
 * SOAP client configuration for the internal
 * compliance validation service.
 *
 * HARDCODED endpoint: the compliance-api runs
 * on 10.192.4.47:8080 (owned by Compliance
 * team). No migration planned — no container
 * or cloud alternative exists. Direct network
 * path required from application host.
 */
@Configuration
public class SoapClientConfig {

    private static final String SOAP_URL =
        "http://10.192.4.47:8080"
        + "/compliance-api/v1/validate";

    @Bean
    public Jaxb2Marshaller complianceMarshaller() {
        Jaxb2Marshaller m = new Jaxb2Marshaller();
        m.setContextPath(
            "com.ubs.docpipeline.model"
        );
        return m;
    }

    @Bean
    public WebServiceTemplate complianceWsTemplate(
            Jaxb2Marshaller complianceMarshaller) {
        WebServiceTemplate ws =
            new WebServiceTemplate();
        ws.setMarshaller(complianceMarshaller);
        ws.setUnmarshaller(complianceMarshaller);
        ws.setDefaultUri(SOAP_URL);
        ws.setMessageSender(
            new HttpComponentsMessageSender()
        );
        return ws;
    }
}
