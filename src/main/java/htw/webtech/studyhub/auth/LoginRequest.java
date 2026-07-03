package htw.webtech.studyhub.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO für den Login-Vorgang eines bestehenden Benutzers.
 *
 * @param email    Die E-Mail-Adresse, mit der sich der Benutzer registriert hat.
 * @param password Das zugehörige Passwort.
 */
public record LoginRequest(
        @NotBlank @Email String email,
        @NotBlank String password
) {
}
