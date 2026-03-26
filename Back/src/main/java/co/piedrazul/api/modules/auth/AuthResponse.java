package co.piedrazul.api.modules.auth;

public record AuthResponse(boolean ok, String token, String role) {}
