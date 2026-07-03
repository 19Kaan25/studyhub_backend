package htw.webtech.studyhub.auth;

import htw.webtech.studyhub.security.JwtService;
import htw.webtech.studyhub.user.User;
import htw.webtech.studyhub.user.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST-Controller für die Benutzerauthentifizierung.
 * Stellt die öffentlichen Endpunkte für die Registrierung neuer Accounts und
 * den Login bestehender Benutzer zur Verfügung.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;

    public AuthController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    /**
     * Registriert einen neuen Benutzer im System und loggt ihn direkt ein.
     *
     * @param request Das validierte {@link RegisterRequest} mit den Benutzerdaten.
     * @return Eine {@link AuthResponse}, die das JWT und grundlegende Nutzerdaten enthält.
     */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        User user = userService.register(request.username(), request.email(), request.password());
        String token = jwtService.generateToken(user.getUsername());
        return new AuthResponse(token, user.getUsername(), user.getId());
    }

    /**
     * Authentifiziert einen bestehenden Benutzer anhand seiner Anmeldedaten.
     *
     * @param request Das validierte {@link LoginRequest} mit E-Mail und Passwort.
     * @return Eine {@link AuthResponse}, die das JWT und grundlegende Nutzerdaten enthält,
     * sofern die Zugangsdaten korrekt sind.
     */
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        User user = userService.authenticate(request.email(), request.password());
        String token = jwtService.generateToken(user.getUsername());
        return new AuthResponse(token, user.getUsername(), user.getId());
    }
}
