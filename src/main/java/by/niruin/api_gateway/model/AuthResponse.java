package by.niruin.api_gateway.model;

public record AuthResponse(String accessToken,
                           String refreshToken,
                           String tokenType,
                           int expiresIn) {
}
