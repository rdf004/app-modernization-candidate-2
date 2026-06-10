package com.ubs.docpipeline.controller;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation
    .Autowired;
import org.springframework.beans.factory.annotation
    .Value;
import org.springframework.data.redis.core
    .RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation
    .GetMapping;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Controller
public class DashboardController {

    @PersistenceContext
    private EntityManager em;

    @Autowired(required = false)
    private RedisTemplate<String, Object>
        redisTemplate;

    @Value("${app.dirs.incoming:"
        + "/data/incoming-docs}")
    private String incomingDir;

    @Value("${app.dirs.processed:"
        + "/data/processed}")
    private String processedDir;

    @Value("${app.dirs.reports:"
        + "/data/reports}")
    private String reportsDir;

    @GetMapping("/")
    public String dashboard(Model model) {
        model.addAttribute(
            "javaVersion",
            System.getProperty(
                "java.version"
            )
        );
        model.addAttribute(
            "springVersion",
            org.springframework.boot
                .SpringBootVersion
                .getVersion()
        );
        model.addAttribute(
            "runtimeInfo",
            buildRuntimeInfo()
        );
        model.addAttribute(
            "dirs",
            buildDirStatus()
        );
        model.addAttribute(
            "recentDocs",
            recentAuditRecords()
        );
        model.addAttribute(
            "processedFiles",
            listFiles(processedDir)
        );
        model.addAttribute(
            "reportFiles",
            listFiles(reportsDir)
        );
        model.addAttribute(
            "redisOk", isRedisOk()
        );
        model.addAttribute(
            "dbOk", isDatabaseOk()
        );
        return "dashboard";
    }

    private Map<String, String>
            buildRuntimeInfo() {
        Map<String, String> info =
            new LinkedHashMap<>();
        info.put(
            "Java",
            System.getProperty(
                "java.version"
            )
        );
        info.put(
            "VM",
            System.getProperty(
                "java.vm.name"
            )
        );
        info.put(
            "Spring Boot",
            org.springframework.boot
                .SpringBootVersion
                .getVersion()
        );
        info.put(
            "OS",
            System.getProperty("os.name")
            + " "
            + System.getProperty(
                "os.version"
            )
        );
        info.put(
            "Arch",
            System.getProperty("os.arch")
        );
        return info;
    }

    private Map<String, String>
            buildDirStatus() {
        Map<String, String> dirs =
            new LinkedHashMap<>();
        dirs.put(
            "Incoming",
            dirStatus(incomingDir)
        );
        dirs.put(
            "Processed",
            dirStatus(processedDir)
        );
        dirs.put(
            "Reports",
            dirStatus(reportsDir)
        );
        return dirs;
    }

    private String dirStatus(String dir) {
        Path p = Paths.get(dir);
        if (!Files.exists(p)) {
            return "NOT FOUND";
        }
        try (Stream<Path> s = Files.list(p)) {
            long count = s.count();
            return count + " files";
        } catch (IOException e) {
            return "ERROR";
        }
    }

    @SuppressWarnings("unchecked")
    private List<Object[]>
            recentAuditRecords() {
        try {
            return em.createQuery(
                "SELECT a.documentId, "
                + "a.action, a.detail, "
                + "a.createdAt "
                + "FROM AuditRecord a "
                + "ORDER BY a.createdAt DESC"
            ).setMaxResults(20)
             .getResultList();
        } catch (RuntimeException e) {
            return Collections.emptyList();
        }
    }

    private List<String> listFiles(
            String dir) {
        Path p = Paths.get(dir);
        if (!Files.exists(p)) {
            return Collections.emptyList();
        }
        try (Stream<Path> s = Files.list(p)) {
            return s
                .map(f ->
                    f.getFileName().toString()
                )
                .sorted()
                .toList();
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }

    private boolean isRedisOk() {
        if (redisTemplate == null) {
            return false;
        }
        try {
            redisTemplate
                .getConnectionFactory()
                .getConnection()
                .ping();
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }

    private boolean isDatabaseOk() {
        try {
            em.createNativeQuery(
                "SELECT 1"
            ).getSingleResult();
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }
}
