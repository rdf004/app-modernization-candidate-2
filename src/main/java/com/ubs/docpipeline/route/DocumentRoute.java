package com.ubs.docpipeline.route;

import com.ubs.docpipeline.processor
    .IncomingDocProcessor;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation
    .Value;
import org.springframework.stereotype.Component;

@Component
public class DocumentRoute
        extends RouteBuilder {

    private final IncomingDocProcessor
        processor;

    @Value("${docpipeline.dirs.incoming:"
        + "/opt/ubs/incoming-docs}")
    private String incomingDir;

    @Value("${docpipeline.dirs.processed:"
        + "/opt/ubs/processed}")
    private String processedDir;

    @Value("${docpipeline.dirs.reports:"
        + "/opt/ubs/reports}")
    private String reportsDir;

    public DocumentRoute(
            IncomingDocProcessor processor) {
        this.processor = processor;
    }

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
                "file://" + incomingDir
                + "/.error"
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
                        "file://"
                        + processedDir
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
                        "file://"
                        + reportsDir
                        + "/flagged"
                    )
                .otherwise()
                    .log("Skipped/failed: "
                        + "${header.docId}")
            .end()
            .log("Route complete for "
                + "${header.CamelFileName}");
    }
}
