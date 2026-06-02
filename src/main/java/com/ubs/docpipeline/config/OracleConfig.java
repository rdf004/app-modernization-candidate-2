package com.ubs.docpipeline.config;

import javax.sql.DataSource;
import org.springframework.beans.factory.annotation
    .Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation
    .Configuration;
import org.springframework.context.annotation
    .Profile;
import org.springframework.boot.jdbc
    .DataSourceBuilder;

@Configuration
public class OracleConfig {

    @Value("${oracle.host:"
        + "ora-auditdb-01.internal}")
    private String host;

    @Value("${oracle.port:1521}")
    private int port;

    @Value("${oracle.sid:UBSAUDIT}")
    private String sid;

    @Value("${oracle.username:doc_pipeline}")
    private String username;

    @Value("${oracle.password:}")
    private String password;

    @Bean
    @Profile("production")
    public DataSource oracleDataSource() {
        String url = String.format(
            "jdbc:oracle:thin:@%s:%d:%s",
            host, port, sid
        );
        return DataSourceBuilder.create()
            .driverClassName(
                "oracle.jdbc.OracleDriver"
            )
            .url(url)
            .username(username)
            .password(password)
            .build();
    }
}
