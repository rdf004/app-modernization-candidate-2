package com.ubs.docpipeline.route;

import com.ubs.docpipeline.processor
    .IncomingDocProcessor;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation
    .Autowired;
import org.springframework.stereotype.Component;

/**
 * Core Camel route for document processing.
 * Receives files from local and NFS ingest
 * routes, processes them, and routes to
 * output directories.
 */
@Component
public class DocumentRoute
        extends RouteBuilder {

    @Autowired
    private IncomingDocProcessor processor;

    @Override
    public void configure() throws Exception {

        onException(Exception.class)
            .handled(true)
            .log(
                LoggingLevel.ERROR,
                "Processing failed: "
                + "${exception.message}"
            )
            .to(
                "file:///opt/ubs/incoming-docs/"
                + ".error"
            );

        from("direct:process-doc")
            .routeId("doc-processing-main")
            .log("Start processing: "
                + "${header.CamelFileName}")
            .process(processor)
            .choice()
                .when(
                    header("docStatus")
                    .isEqualTo("COMPLIANT")
                )
                    .log("Compliant: "
                        + "${header.docId}")
                    .to(
                        "file:///opt/ubs/"
                        + "processed"
                    )
                .when(
                    header("docStatus")
                    .isEqualTo(
                        "NON_COMPLIANT"
                    )
                )
                    .log("Non-compliant: "
                        + "${header.docId}")
                    .to(
                        "file:///opt/ubs/"
                        + "reports/flagged"
                    )
                .otherwise()
                    .log("Skipped/failed: "
                        + "${header.docId}")
            .end()
            .log("Route complete for "
                + "${header.CamelFileName}");
    }
}
