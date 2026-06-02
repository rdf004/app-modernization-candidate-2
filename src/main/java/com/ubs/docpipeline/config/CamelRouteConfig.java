package com.ubs.docpipeline.config;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation
    .Configuration;

@Configuration
public class CamelRouteConfig {

    private static final String INCOMING =
        "file:///opt/ubs/incoming-docs/"
        + "?include=.*\\.(pdf|csv)"
        + "&move=.done"
        + "&moveFailed=.error"
        + "&readLock=changed"
        + "&readLockCheckInterval=1000"
        + "&readLockTimeout=30000";

    private static final String NFS_INCOMING =
        "file:///mnt/shared-docs/incoming/"
        + "?include=.*\\.(pdf|csv)"
        + "&move=.done"
        + "&readLock=fileLock"
        + "&readLockTimeout=60000";

    @Bean
    public RouteBuilder localFileRoute() {
        return new RouteBuilder() {
            @Override
            public void configure() {
                from(INCOMING)
                    .routeId("local-doc-ingest")
                    .log("Received: "
                        + "${header.CamelFileName}")
                    .to("direct:process-doc");
            }
        };
    }

    @Bean
    public RouteBuilder nfsFileRoute() {
        return new RouteBuilder() {
            @Override
            public void configure() {
                from(NFS_INCOMING)
                    .routeId("nfs-doc-ingest")
                    .log("NFS file: "
                        + "${header.CamelFileName}")
                    .to("direct:process-doc");
            }
        };
    }
}
