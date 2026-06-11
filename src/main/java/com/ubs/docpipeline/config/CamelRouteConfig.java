package com.ubs.docpipeline.config;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation
    .Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation
    .Configuration;

@Configuration
public class CamelRouteConfig {

    @Value("${docpipeline.dirs.incoming:"
        + "/opt/ubs/incoming-docs}")
    private String incomingDir;

    @Value("${docpipeline.dirs.nfs-incoming:"
        + "/mnt/shared-docs/incoming}")
    private String nfsIncomingDir;

    @Bean
    public RouteBuilder localFileRoute() {
        String uri = "file://" + incomingDir
            + "?include=.*\\.(pdf|csv)"
            + "&move=.done"
            + "&moveFailed=.error"
            + "&readLock=changed"
            + "&readLockCheckInterval=1000"
            + "&readLockTimeout=30000";
        return new RouteBuilder() {
            @Override
            public void configure() {
                from(uri)
                    .routeId(
                        "local-doc-ingest"
                    )
                    .log("Received: "
                        + "${header"
                        + ".CamelFileName}")
                    .to("direct:process-doc");
            }
        };
    }

    @Bean
    public RouteBuilder nfsFileRoute() {
        String uri = "file://" + nfsIncomingDir
            + "?include=.*\\.(pdf|csv)"
            + "&move=.done"
            + "&readLock=fileLock"
            + "&readLockTimeout=60000";
        return new RouteBuilder() {
            @Override
            public void configure() {
                from(uri)
                    .routeId(
                        "nfs-doc-ingest"
                    )
                    .log("NFS file: "
                        + "${header"
                        + ".CamelFileName}")
                    .to("direct:process-doc");
            }
        };
    }
}
