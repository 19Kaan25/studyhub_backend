package htw.webtech.studyhub.post;

import htw.webtech.studyhub.user.User;
import htw.webtech.studyhub.user.UserService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/posts/{postId}/file")
public class PostFileController {

    private final PostFileService fileService;
    private final UserService userService;

    public PostFileController(PostFileService fileService, UserService userService) {
        this.fileService = fileService;
        this.userService = userService;
    }

    /** Datei zu einem Post hochladen (nur Eigentümer). */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void upload(@PathVariable Long postId,
                       @RequestParam("file") MultipartFile file,
                       @AuthenticationPrincipal UserDetails principal) {
        fileService.store(postId, file, currentUserId(principal));
    }

    /** Datei eines Posts herunterladen (öffentlich, wie das Lesen der Posts). */
    @GetMapping
    public ResponseEntity<byte[]> download(@PathVariable Long postId) {
        PostFile postFile = fileService.get(postId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + postFile.getFileName() + "\"")
                .contentType(MediaType.parseMediaType(postFile.getContentType()))
                .body(postFile.getData());
    }

    private Long currentUserId(UserDetails principal) {
        return userService.findByUsername(principal.getUsername())
                .map(User::getId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unbekannter Benutzer"));
    }
}
