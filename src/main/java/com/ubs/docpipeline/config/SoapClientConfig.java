package com.ubs.docpipeline.config;

import org.springframework.beans.factory.annotation
    .Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation
    .Configuration;
import org.springframework.oxm.jaxb
    .Jaxb2Marshaller;
import org.springframework.ws.client.core
    .WebServiceTemplate;
import org.springframework.ws.transport.http
    .HttpComponentsMessageSender;

@Configuration
public class SoapClientConfig {

    @Value("${docpipeline.compliance.soap-url:"
        + "http://10.192.4.47:8080"
        + "/compliance-api/v1/validate}")
    private String soapUrl;

    @Bean
    public Jaxb2Marshaller
            complianceMarshaller() {
        Jaxb2Marshaller m =
            new Jaxb2Marshaller();
        m.setContextPath(
            "com.ubs.docpipeline.model"
        );
        return m;
    }

    @Bean
    public WebServiceTemplate
            complianceWsTemplate(
                Jaxb2Marshaller
                    complianceMarshaller) {
        WebServiceTemplate ws =
            new WebServiceTemplate();
        ws.setMarshaller(complianceMarshaller);
        ws.setUnmarshaller(
            complianceMarshaller
        );
        ws.setDefaultUri(soapUrl);
        ws.setMessageSender(
            new HttpComponentsMessageSender()
        );
        return ws;
    }
}
