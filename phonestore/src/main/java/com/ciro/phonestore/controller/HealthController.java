package com.ciro.phonestore.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthController {

    private static final Logger logger = LoggerFactory.getLogger(HealthController.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/actuator/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> details = new HashMap<>();

        try {

            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            details.put("database", "UP");


            Runtime runtime = Runtime.getRuntime();
            Map<String, Object> memory = new HashMap<>();
            memory.put("total", runtime.totalMemory());
            memory.put("free", runtime.freeMemory());
            memory.put("used", runtime.totalMemory() - runtime.freeMemory());
            details.put("memory", memory);

            response.put("status", "UP");
            response.put("details", details);

            logger.debug("Health check passed successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Health check failed", e);
            details.put("database", "DOWN");
            response.put("status", "DOWN");
            response.put("details", details);
            response.put("error", e.getMessage());
            return ResponseEntity.status(503).body(response);
        }
    }
}