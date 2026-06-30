package htw.webtech.studyhub.post;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PostService {

    private final PostRepository repo;

    public PostService(PostRepository repo) {
        this.repo = repo;
    }

    public Iterable<Post> getAll() {
        return repo.findAll();
    }

    public Post get(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post nicht gefunden: " + id));
    }

    public Post create(Post post, Long userId) {
        // userId kommt immer aus dem Token, nie aus dem Request-Body.
        post.setUserId(userId);
        return repo.save(post);
    }

    public Post update(Long id, Post data, Long userId) {
        Post existing = get(id);
        requireOwner(existing, userId);

        existing.setTitle(data.getTitle());
        existing.setContent(data.getContent());
        existing.setUrl(data.getUrl());
        existing.setType(data.getType());
        existing.setPreviewTitle(data.getPreviewTitle());
        existing.setPreviewDescription(data.getPreviewDescription());
        existing.setPreviewImageUrl(data.getPreviewImageUrl());
        return repo.save(existing);
    }

    public void delete(Long id, Long userId) {
        Post existing = get(id);
        requireOwner(existing, userId);
        repo.delete(existing);
    }

    private void requireOwner(Post post, Long userId) {
        if (post.getUserId() == null || !post.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Kein Zugriff auf fremde Posts.");
        }
    }
}
