package dev.mam.buizsol.mamshop.customer.service;

import dev.mam.buizsol.mamshop.customer.exception.CustomerValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class KeycloakAdminService {

    private final RestClient restClient;
    private final String realm;
    private final String adminUser;
    private final String adminPass;

    public KeycloakAdminService(
            @Value("${app.keycloak.internal-url:http://keycloak:8080}") final String internalUrl,
            @Value("${app.keycloak.realm:mail-and-media-shop-realm}") final String realm,
            @Value("${app.keycloak.admin-user:admin}") final String adminUser,
            @Value("${app.keycloak.admin-pass:admin}") final String adminPass) {
        this.realm = realm;
        this.adminUser = adminUser;
        this.adminPass = adminPass;
        this.restClient = RestClient.builder().baseUrl(internalUrl).build();
    }

    @SuppressWarnings("unchecked")
    private String getAdminAccessToken() {
        log.debug("Requesting Keycloak admin access token for user: {}", adminUser);
        final MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "password");
        body.add("client_id", "admin-cli");
        body.add("username", adminUser);
        body.add("password", adminPass);

        try {
            final Map<String, Object> response = restClient
                    .post()
                    .uri("/realms/master/protocol/openid-connect/token")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(body)
                    .retrieve()
                    .body(Map.class);

            if (response != null && response.containsKey("access_token")) {
                return (String) response.get("access_token");
            }
            throw new CustomerValidationException("Failed to retrieve admin token from Keycloak: response empty");
        } catch (final Exception e) {
            log.error("Failed to obtain Keycloak admin token", e);
            throw new CustomerValidationException("Keycloak connection error: " + e.getMessage());
        }
    }

    public UUID createUser(final String email, final String firstName, final String lastName, final String password) {
        log.info("Creating user in Keycloak: {}", email);
        final String adminToken = getAdminAccessToken();

        final Map<String, Object> credential = Map.of("type", "password", "value", password, "temporary", false);

        final Map<String, Object> userRequest = Map.of(
                "username", email,
                "email", email,
                "enabled", true,
                "firstName", firstName,
                "lastName", lastName,
                "credentials", List.of(credential));

        try {
            final ResponseEntity<Void> response = restClient
                    .post()
                    .uri("/admin/realms/{realm}/users", realm)
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(userRequest)
                    .retrieve()
                    .toBodilessEntity();

            if (response.getStatusCode().is2xxSuccessful()) {
                final String location = response.getHeaders().getFirst("Location");
                log.debug("User created in Keycloak successfully, Location header: {}", location);
                if (location != null) {
                    final String userIdStr = location.substring(location.lastIndexOf("/") + 1);
                    return UUID.fromString(userIdStr);
                }
                throw new CustomerValidationException("Keycloak user created, but Location header is missing");
            }
            throw new CustomerValidationException("Keycloak returned status: " + response.getStatusCode());
        } catch (final HttpClientErrorException.Conflict e) {
            log.warn("Keycloak conflict - user already exists: {}", email);
            throw new CustomerValidationException("User with email " + email + " already exists in Keycloak");
        } catch (final Exception e) {
            log.error("Failed to create user in Keycloak", e);
            throw new CustomerValidationException("Failed to register user in identity provider: " + e.getMessage());
        }
    }
}
