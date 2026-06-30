package htw.webtech.studyhub.user;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository repo;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository repo, PasswordEncoder passwordEncoder) {
        this.repo = repo;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registriert einen neuen User. Das Passwort wird mit BCrypt gehasht.
     * Wirft 409 CONFLICT, wenn E-Mail oder Username bereits vergeben sind.
     */
    public User register(String username, String email, String rawPassword) {
        if (repo.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "E-Mail ist bereits registriert.");
        }
        if (repo.existsByUsername(username)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username ist bereits vergeben.");
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        return repo.save(user);
    }

    /**
     * Prüft die Anmeldedaten. Bei Erfolg wird der User zurückgegeben,
     * sonst 401 UNAUTHORIZED (ohne zu verraten, ob E-Mail oder Passwort falsch war).
     */
    public User authenticate(String email, String rawPassword) {
        User user = repo.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Ungültige Anmeldedaten."));

        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Ungültige Anmeldedaten.");
        }
        return user;
    }

    public Optional<User> findByEmail(String email) {
        return repo.findByEmail(email);
    }

    public Optional<User> findByUsername(String username) {
        return repo.findByUsername(username);
    }
}
