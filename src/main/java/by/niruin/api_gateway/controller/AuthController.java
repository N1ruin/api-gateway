package by.niruin.api_gateway.controller;

import by.niruin.api_gateway.client.KeycloakClient;
import by.niruin.api_gateway.config.KeycloakProperties;
import by.niruin.api_gateway.model.AuthResponse;
import by.niruin.api_gateway.model.LoginRequest;
import by.niruin.api_gateway.model.RefreshRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class AuthController {
    private final KeycloakClient keycloakClient;
    private final KeycloakProperties keycloakProperties;

    public AuthController(KeycloakClient keycloakClient, KeycloakProperties keycloakProperties) {
        this.keycloakClient = keycloakClient;
        this.keycloakProperties = keycloakProperties;
    }


    @PostMapping("/login")
    public Mono<AuthResponse> login(@RequestBody LoginRequest request) {
        var body = """
                grant_type=password&
                client_id=%s&
                client_secret=%s&
                username=%s&
                password=%s
                """.formatted(
                keycloakProperties.getClientId(),
                keycloakProperties.getClientSecret(),
                request.username(),
                request.password()
        ).replaceAll("\\s+", "");

        return keycloakClient.login(body);
    }

    @PostMapping("/refresh")
    public Mono<AuthResponse> refresh(@RequestBody RefreshRequest request) {
        var body = """
                grant_type=refresh_token&
                client_id=%s&
                client_secret=%s&
                refresh_token=%s
                """.formatted(
                keycloakProperties.getClientId(),
                keycloakProperties.getClientSecret(),
                request.refreshToken()
        ).replaceAll("\\s+", "");

        return keycloakClient.refresh(body);
    }
}
