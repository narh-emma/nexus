package com.nexus.adminservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Admin Console service.
 *
 * Sits alongside Auth, News, Media and Translate behind the API Gateway.
 * Responsibilities (per Backend Plan, sections 2.1 / 7):
 *   - User management (list, disable/enable, role changes)
 *   - Audit log read access (admin mutations + translation events)
 *   - Aggregate stats for the admin web console
 */
@SpringBootApplication
public class AdminServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AdminServiceApplication.class, args);
    }
}
