package com.ubs.docpipeline.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation
    .Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation
    .Configuration;
import org.springframework.ws.client.core
    .WebServiceTemplate;

@Configuration
public class SoapClientConfig {

    private static final Logger LOG =
        LoggerFactory.getLogger(
            SoapClientConfig.class
        );

    @Value("${compliance.soap.url:"
        + "http://localhost:8081"
        + "/compliance-api/v1/validate}")
    private String soapUrl;

    @Bean
    public WebServiceTemplate
            complianceWsTemplate() {
        WebServiceTemplate ws =
            new WebServiceTemplate();
        ws.setDefaultUri(soapUrl);
        LOG.info(
            "SOAP compliance endpoint: {}",
            soapUrl
        );
        return ws;
    }
}
