package htw.webtech.studyhub.favorite;

import htw.webtech.studyhub.post.Post;
import htw.webtech.studyhub.post.PostRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FavoriteService {

    private final FavoriteRepository repo;
    private final PostRepository postRepo;

    public FavoriteService(FavoriteRepository repo, PostRepository postRepo) {
        this.repo = repo;
        this.postRepo = postRepo;
    }

    /** Markiert einen Post als Favorit. Doppelte Markierungen werden ignoriert. */
    public void add(Long postId, Long userId) {
        if (!postRepo.existsById(postId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Post nicht gefunden: " + postId);
        }
        // Schon favorisiert? Dann nichts tun (idempotent).
        if (repo.existsByUserIdAndPostId(userId, postId)) {
            return;
        }
        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setPostId(postId);
        repo.save(favorite);
    }

    /** Entfernt die Favoriten-Markierung, falls vorhanden. */
    public void remove(Long postId, Long userId) {
        repo.findByUserIdAndPostId(userId, postId).ifPresent(repo::delete);
    }

    /** Liefert die favorisierten Posts eines Users, zuletzt markierte zuerst. */
    public List<Post> getFavoritePostsForUser(Long userId) {
        List<Favorite> favorites = repo.findByUserIdOrderByCreatedAtDesc(userId);

        // Posts in einem Rutsch laden und über eine Map der Favoriten-Reihenfolge zuordnen.
        List<Long> postIds = favorites.stream().map(Favorite::getPostId).toList();
        Map<Long, Post> postsById = new HashMap<>();
        postRepo.findAllById(postIds).forEach(post -> postsById.put(post.getId(), post));

        List<Post> result = new ArrayList<>();
        for (Favorite favorite : favorites) {
            Post post = postsById.get(favorite.getPostId());
            // Falls ein Post inzwischen gelöscht wurde, aber die Markierung noch existiert.
            if (post != null) {
                result.add(post);
            }
        }
        return result;
    }
}
