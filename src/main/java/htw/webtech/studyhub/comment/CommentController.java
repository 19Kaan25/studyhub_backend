package htw.webtech.studyhub.comment;

import htw.webtech.studyhub.user.User;
import htw.webtech.studyhub.user.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * REST-Controller für die Kommentare eines Posts.
 * Lesen ist öffentlich, Schreiben und Löschen erfordern ein gültiges Token.
 */
@RestController
@RequestMapping("/api/posts/{postId}/comments")
public class CommentController {

    private final CommentService service;
    private final UserService userService;
    Logger logger = LoggerFactory.getLogger(CommentController.class);

    public CommentController(CommentService service, UserService userService) {
        this.service = service;
        this.userService = userService;
    }

    /** Alle Kommentare eines Posts abrufen (öffentlich lesbar). */
    @GetMapping
    public List<Comment> getComments(@PathVariable Long postId) {
        return service.getForPost(postId);
    }

    /** Neuen Kommentar zu einem Post erstellen (nur eingeloggt). */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Comment createComment(@PathVariable Long postId,
                                 @Valid @RequestBody CommentRequest request,
                                 @AuthenticationPrincipal UserDetails principal) {
        User user = currentUser(principal);
        Comment comment = service.create(postId, request.content(), user.getId(), user.getUsername());
        logger.info("Kommentar {} zu Post {} von User {} erstellt", comment.getId(), postId, user.getId());
        return comment;
    }

    /** Eigenen Kommentar löschen (Autoren-Check im Service). */
    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable Long postId,
                              @PathVariable Long commentId,
                              @AuthenticationPrincipal UserDetails principal) {
        service.delete(commentId, currentUser(principal).getId());
        logger.info("Kommentar {} zu Post {} wurde gelöscht", commentId, postId);
    }

    /** Liest den eingeloggten Benutzer aus dem Security-Kontext (JWT). */
    private User currentUser(UserDetails principal) {
        return userService.findByUsername(principal.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unbekannter Benutzer"));
    }
}
