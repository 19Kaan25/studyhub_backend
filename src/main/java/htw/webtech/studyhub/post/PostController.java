package htw.webtech.studyhub.post;

import htw.webtech.studyhub.user.User;
import htw.webtech.studyhub.user.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * REST-Controller für die Verwaltung von Beiträgen (Posts).
 * Stellt die API-Endpunkte für den Create, Read, Update, Delete-Zyklus
 * von Posts bereit. Verknüpft sicherheitsrelevante Aktionen (Erstellen, Ändern, Löschen)
 * automatisch mit dem aktuell authentifizierten Benutzer.
 */
@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService service;
    private final UserService userService;

    public PostController(PostService service, UserService userService) {
        this.service = service;
        this.userService = userService;
    }

    /**
     * Ruft einen globalen Feed aller vorhandenen Beiträge ab.
     * @return Eine Liste (Iterable) aller Posts auf der Plattform.
     */
    @GetMapping
    public Iterable<Post> getAllPosts() {
        return service.getAll();
    }

    /**
     * Ruft nur die Posts des eingeloggten Users (für den "Meine Posts"-Reiter).
     * @param principal Das Security-Objekt mit den Daten des eingeloggten Benutzers (aus dem JWT).
     * @return Eine Liste der Beiträge, die diesem Benutzer gehören.
     */
    @GetMapping("/mine")
    public Iterable<Post> getMyPosts(@AuthenticationPrincipal UserDetails principal) {
        return service.getByUser(currentUserId(principal));
    }

    /**
     * Ruft die Details eines spezifischen Beitrags anhand seiner ID ab.
     * @param id Die eindeutige ID des gesuchten Posts.
     * @return Der zu der ID zugehörige Post.
     */
    @GetMapping("/{id}")
    public Post getPost(@PathVariable Long id) {
        return service.get(id);
    }

    /**
     * Erstellt einen neuen Beitrag und verknüpft diesen automatisch mit dem
     * Account des aktuell eingeloggten Benutzers.
     * @param post      Die validierten Daten des neuen Beitrags (aus dem Request-Body).
     * @param principal Das Security-Objekt des eingeloggten Benutzers.
     * @return Den gerade gespeicherten Post inklusive generierter Datenbank-ID.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Post createPost(@Valid @RequestBody Post post, @AuthenticationPrincipal UserDetails principal) {
        return service.create(post, currentUserId(principal));
    }

    /**
     * Aktualisiert die Daten eines bestehenden Beitrags.
     * Prüft ob Post vom eingeloggten User angelegt wurde.
     * @param id        Die ID des zu aktualisierenden Posts.
     * @param post      Die neuen Beitragsdaten.
     * @param principal Das Security-Objekt des ausführenden Benutzers.
     * @return Der erfolgreich aktualisierte Post.
     */
    @PutMapping("/{id}")
    public Post updatePost(@PathVariable Long id,
                           @Valid @RequestBody Post post,
                           @AuthenticationPrincipal UserDetails principal) {
        return service.update(id, post, currentUserId(principal));
    }

    /**
     * Löscht einen bestehenden Beitrag.
     * Prüft ob Post vom eingeloggten User angelegt wurde.
     * @param id        Die ID des zu löschenden Posts.
     * @param principal Das Security-Objekt des ausführenden Benutzers.
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePost(@PathVariable Long id, @AuthenticationPrincipal UserDetails principal) {
        service.delete(id, currentUserId(principal));
    }

    /**
     * Hilfsmethode: Liest den Benutzernamen aus dem aktuellen Security-Kontext (JWT) aus
     * und liefert dessen DB-Id.
     * @param principal Das Security-Objekt der aktuellen Anfrage.
     * @return Die eindeutige ID des Benutzers.
     * @throws ResponseStatusException (401 UNAUTHORIZED), falls der im Token hinterlegte
     * Benutzer nicht mehr in der Datenbank existiert.
     */
    private Long currentUserId(UserDetails principal) {
        return userService.findByUsername(principal.getUsername())
                .map(User::getId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unbekannter Benutzer"));
    }
}
