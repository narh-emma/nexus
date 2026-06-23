package com.nexus.adminservice.client;

import com.nexus.adminservice.dto.AdminUserView;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * Talks to the Auth Service over HTTP (never reads the users table directly).
 * This keeps the "no hard foreign keys across services" rule from §4.2 intact —
 * Admin only ever sees what Auth chooses to expose, and only with a valid
 * service-to-service token.
 */
@Component
public class AuthServiceClient {

    private final RestTemplate restTemplate;

    @Value("${nexus.auth-service.base-url}")
    private String authServiceBaseUrl;

    public AuthServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<AdminUserView> fetchAllUsers(String bearerToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", bearerToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        AdminUserView[] users = restTemplate.exchange(
                authServiceBaseUrl + "/internal/users",
                HttpMethod.GET,
                entity,
                AdminUserView[].class
        ).getBody();

        return users == null ? List.of() : List.of(users);
    }

    public void setUserEnabled(String userId, boolean enabled, String bearerToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", bearerToken);
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>("{\"enabled\":" + enabled + "}", headers);

        restTemplate.exchange(
                authServiceBaseUrl + "/internal/users/" + userId + "/status",
                HttpMethod.PATCH,
                entity,
                Void.class
        );
    }
}
