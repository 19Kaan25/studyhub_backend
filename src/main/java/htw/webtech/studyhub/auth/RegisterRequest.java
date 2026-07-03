package htw.webtech.studyhub.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO für die Registrierung eines neuen Benutzers.
 *
 * @param username Der gewählte Benutzername (muss zwischen 3 und 50 Zeichen lang sein).
 * @param email    Die gültige E-Mail-Adresse des Benutzers.
 * @param password Das gewählte Passwort (muss zwischen 6 und 100 Zeichen lang sein).
 */
public record RegisterRequest(
        @NotBlank @Size(min = 3, max = 50) String username,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 6, max = 100) String password
) {
}
