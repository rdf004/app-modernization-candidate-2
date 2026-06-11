package com.ubs.docpipeline;

import org.springframework.beans.factory.annotation
    .Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure
    .SpringBootApplication;
import org.springframework.scheduling.annotation
    .EnableScheduling;

import jakarta.annotation.PostConstruct;

@SpringBootApplication
@EnableScheduling
public class Application {

    @Value("${docpipeline.native.lib-path:"
        + "/usr/local/lib}")
    private String nativeLibPath;

    @PostConstruct
    public void setLibraryPath() {
        System.setProperty(
            "java.library.path",
            nativeLibPath
        );
    }

    public static void main(String[] args) {
        SpringApplication.run(
            Application.class, args
        );
    }
}
