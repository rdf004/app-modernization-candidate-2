package com.ubs.docpipeline.processor;

import com.ubs.docpipeline.model
    .ProcessedDocument;
import com.ubs.docpipeline.service
    .DocumentProcessorService;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class IncomingDocProcessor
        implements Processor {

    private static final Logger LOG =
        LoggerFactory.getLogger(
            IncomingDocProcessor.class
        );

    private final DocumentProcessorService
        docSvc;

    public IncomingDocProcessor(
            DocumentProcessorService docSvc) {
        this.docSvc = docSvc;
    }

    @Override
    public void process(Exchange exchange)
            throws Exception {
        File file = exchange.getIn()
            .getBody(File.class);

        if (file == null) {
            LOG.error(
                "Null file in exchange"
            );
            return;
        }

        LOG.info(
            "Processing file: {}",
            file.getName()
        );

        ProcessedDocument result =
            docSvc.process(file);

        if (result != null) {
            exchange.getIn().setBody(result);
            exchange.getIn().setHeader(
                "docStatus",
                result.getStatus().name()
            );
            exchange.getIn().setHeader(
                "docId",
                result.getDocumentId()
            );
        } else {
            exchange.getIn().setHeader(
                "docStatus",
                "SKIPPED"
            );
        }
    }
}
