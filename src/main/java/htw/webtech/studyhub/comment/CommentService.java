package htw.webtech.studyhub.comment;

import htw.webtech.studyhub.post.PostRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CommentService {

    private final CommentRepository repo;
    private final PostRepository postRepo;

    public CommentService(CommentRepository repo, PostRepository postRepo) {
        this.repo = repo;
        this.postRepo = postRepo;
    }

    /** Alle Kommentare eines Posts (älteste zuerst). */
    public List<Comment> getForPost(Long postId) {
        return repo.findByPostIdOrderByCreatedAtAsc(postId);
    }

    /** Legt einen neuen Kommentar an. Autor und Post kommen nicht aus dem Request-Body. */
    public Comment create(Long postId, String content, Long userId, String username) {
        // Nur kommentieren, wenn der Post auch wirklich existiert.
        if (!postRepo.existsById(postId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Post nicht gefunden: " + postId);
        }

        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setAuthorUsername(username);
        comment.setContent(content);
        return repo.save(comment);
    }

    /** Löscht einen Kommentar, aber nur der Autor darf das. */
    public void delete(Long commentId, Long userId) {
        Comment comment = repo.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Kommentar nicht gefunden: " + commentId));

        if (comment.getUserId() == null || !comment.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Kein Zugriff auf fremde Kommentare.");
        }
        repo.delete(comment);
    }
}
