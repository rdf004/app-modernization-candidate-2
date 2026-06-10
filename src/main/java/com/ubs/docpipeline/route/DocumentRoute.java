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

    private final IncomingDocProcessor processor;
    private final String errorDir;

    public DocumentRoute(
            IncomingDocProcessor processor,
            @Value("${app.dirs.incoming:"
                + "/opt/ubs/incoming-docs}")
            String incomingDir) {
        this.processor = processor;
        this.errorDir =
            "file://" + incomingDir + "/.error";
    }

    @Override
    public void configure() {

        onException(Exception.class)
            .handled(true)
            .log(
                LoggingLevel.ERROR,
                "Processing failed: "
                + "${exception.message}"
            )
            .to(errorDir);

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
                    .to("direct:processed")
                .when(
                    header("docStatus")
                    .isEqualTo(
                        "NON_COMPLIANT"
                    )
                )
                    .log("Non-compliant: "
                        + "${header.docId}")
                    .to("direct:flagged")
                .otherwise()
                    .log(
                        "Skipped/failed: "
                        + "${header.docId}"
                    )
            .end()
            .log("Route complete for "
                + "${header.CamelFileName}");
    }
}
