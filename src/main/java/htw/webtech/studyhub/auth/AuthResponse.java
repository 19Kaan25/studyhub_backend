package htw.webtech.studyhub.auth;

/**
 * DTO (Data Transfer Object) für die Antwort des Servers nach einer erfolgreichen Authentifizierung
 * (sei es durch Registrierung oder Login).
 *
 * @param token    Das generierte JWT (JSON Web Token), das für nachfolgende API-Anfragen benötigt wird.
 * @param username Der Name des erfolgreich authentifizierten Benutzers.
 * @param userId   Die eindeutige Datenbank-ID des Benutzers.
 */
public record AuthResponse(String token, String username, Long userId) {
}
