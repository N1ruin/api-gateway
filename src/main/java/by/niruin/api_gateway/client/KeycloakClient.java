package by.niruin.api_gateway.client;

import by.niruin.api_gateway.config.KeycloakProperties;
import by.niruin.api_gateway.model.AuthResponse;
import by.niruin.api_gateway.model.KeycloakTokenResponse;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class KeycloakClient {
    private final WebClient webClient;
    private final KeycloakProperties keycloakProperties;

    public KeycloakClient(WebClient webClient, KeycloakProperties keycloakProperties) {
        this.webClient = webClient;
        this.keycloakProperties = keycloakProperties;
    }

    public Mono<AuthResponse> login(String body) {
        return webClient.post()
                .uri(keycloakProperties.getTokenUri())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(body)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class)
                                .flatMap(error -> Mono.error(new RuntimeException("Invalid credentials"))))
                .bodyToMono(KeycloakTokenResponse.class)
                .map(kcResponse ->
                        new AuthResponse(kcResponse.accessToken(),
                                kcResponse.refreshToken(),
                                kcResponse.tokenType(),
                                kcResponse.expiresIn()))
                .onErrorResume(error ->
                        Mono.error(new RuntimeException("Authentication failed: " + error.getMessage())));
    }

    public Mono<AuthResponse> refresh(String body) {
        return webClient.post()
                .uri(keycloakProperties.getTokenUri())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(body)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class)
                                .flatMap(error -> Mono.error(new RuntimeException("Failed to refresh token"))))
                .bodyToMono(KeycloakTokenResponse.class)
                .map(kcResponse ->
                        new AuthResponse(kcResponse.accessToken(),
                                kcResponse.refreshToken(),
                                kcResponse.tokenType(),
                                kcResponse.expiresIn()))
                .onErrorResume(error ->
                        Mono.error(new RuntimeException("Token refresh failed: " + error.getMessage())));
    }
}
