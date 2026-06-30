package htw.webtech.studyhub.auth;

public record AuthResponse(String token, String username, Long userId) {
}
