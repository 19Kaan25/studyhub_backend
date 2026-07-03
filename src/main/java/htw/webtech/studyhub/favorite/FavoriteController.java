package htw.webtech.studyhub.favorite;

import htw.webtech.studyhub.post.Post;
import htw.webtech.studyhub.user.User;
import htw.webtech.studyhub.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * REST-Controller für Favoriten. Alle Endpunkte erfordern ein Token, da Favoriten
 * immer an den eingeloggten Benutzer gebunden sind.
 */
@RestController
public class FavoriteController {

    private final FavoriteService service;
    private final UserService userService;
    Logger logger = LoggerFactory.getLogger(FavoriteController.class);

    public FavoriteController(FavoriteService service, UserService userService) {
        this.service = service;
        this.userService = userService;
    }

    /** Einen Post als Favorit markieren. */
    @PostMapping("/api/posts/{postId}/favorite")
    @ResponseStatus(HttpStatus.CREATED)
    public void addFavorite(@PathVariable Long postId, @AuthenticationPrincipal UserDetails principal) {
        Long userId = currentUserId(principal);
        service.add(postId, userId);
        logger.info("Post {} von User {} favorisiert", postId, userId);
    }

    /** Die Favoriten-Markierung eines Posts wieder entfernen. */
    @DeleteMapping("/api/posts/{postId}/favorite")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeFavorite(@PathVariable Long postId, @AuthenticationPrincipal UserDetails principal) {
        Long userId = currentUserId(principal);
        service.remove(postId, userId);
        logger.info("Favorit für Post {} von User {} entfernt", postId, userId);
    }

    /** Alle favorisierten Posts des eingeloggten Users (für die "Favoriten"-Seite). */
    @GetMapping("/api/favorites/mine")
    public List<Post> getMyFavorites(@AuthenticationPrincipal UserDetails principal) {
        return service.getFavoritePostsForUser(currentUserId(principal));
    }

    private Long currentUserId(UserDetails principal) {
        return userService.findByUsername(principal.getUsername())
                .map(User::getId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unbekannter Benutzer"));
    }
}
