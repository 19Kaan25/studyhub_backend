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

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService service;
    private final UserService userService;

    public PostController(PostService service, UserService userService) {
        this.service = service;
        this.userService = userService;
    }

    @GetMapping
    public Iterable<Post> getAllPosts() {
        return service.getAll();
    }

    /** Nur die Posts des eingeloggten Users (für den "Meine Posts"-Reiter). */
    @GetMapping("/mine")
    public Iterable<Post> getMyPosts(@AuthenticationPrincipal UserDetails principal) {
        return service.getByUser(currentUserId(principal));
    }

    @GetMapping("/{id}")
    public Post getPost(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Post createPost(@Valid @RequestBody Post post, @AuthenticationPrincipal UserDetails principal) {
        return service.create(post, currentUserId(principal));
    }

    @PutMapping("/{id}")
    public Post updatePost(@PathVariable Long id,
                           @Valid @RequestBody Post post,
                           @AuthenticationPrincipal UserDetails principal) {
        return service.update(id, post, currentUserId(principal));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePost(@PathVariable Long id, @AuthenticationPrincipal UserDetails principal) {
        service.delete(id, currentUserId(principal));
    }

    /** Liest den eingeloggten User aus dem JWT (via SecurityContext) und liefert dessen DB-Id. */
    private Long currentUserId(UserDetails principal) {
        return userService.findByUsername(principal.getUsername())
                .map(User::getId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unbekannter Benutzer"));
    }
}
