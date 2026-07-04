package htw.webtech.studyhub.favorite;

import htw.webtech.studyhub.post.Post;
import htw.webtech.studyhub.post.PostRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit-Tests für {@link FavoriteService}. Repositories sind gemockt.
 */
@ExtendWith(MockitoExtension.class)
class FavoriteServiceTest {

    @Mock
    private FavoriteRepository repo;
    @Mock
    private PostRepository postRepo;

    @InjectMocks
    private FavoriteService service;

    private Favorite favoritFuer(Long postId) {
        Favorite favorite = new Favorite();
        favorite.setUserId(42L);
        favorite.setPostId(postId);
        return favorite;
    }

    private Post postMitId(Long id) {
        Post post = new Post();
        post.setId(id);
        return post;
    }

    @Test
    void add_neuerFavorit_wirdGespeichert() {
        when(postRepo.existsById(5L)).thenReturn(true);
        when(repo.existsByUserIdAndPostId(42L, 5L)).thenReturn(false);

        service.add(5L, 42L);

        verify(repo).save(any(Favorite.class));
    }

    @Test
    void add_unbekannterPost_wirft404() {
        when(postRepo.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> service.add(99L, 42L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("statusCode").isEqualTo(HttpStatus.NOT_FOUND);

        verify(repo, never()).save(any(Favorite.class));
    }

    @Test
    void add_bereitsFavorisiert_speichertNichtDoppelt() {
        when(postRepo.existsById(5L)).thenReturn(true);
        when(repo.existsByUserIdAndPostId(42L, 5L)).thenReturn(true);

        service.add(5L, 42L);

        verify(repo, never()).save(any(Favorite.class));
    }

    @Test
    void remove_vorhandenerFavorit_wirdGeloescht() {
        Favorite favorite = favoritFuer(5L);
        when(repo.findByUserIdAndPostId(42L, 5L)).thenReturn(Optional.of(favorite));

        service.remove(5L, 42L);

        verify(repo).delete(favorite);
    }

    @Test
    void getFavoritePostsForUser_behaeltReihenfolgeDerFavoriten() {
        // Favoriten in Reihenfolge: erst Post 2, dann Post 1 (neueste zuerst)
        when(repo.findByUserIdOrderByCreatedAtDesc(42L))
                .thenReturn(List.of(favoritFuer(2L), favoritFuer(1L)));
        // findAllById liefert die Posts in beliebiger Reihenfolge
        when(postRepo.findAllById(List.of(2L, 1L)))
                .thenReturn(List.of(postMitId(1L), postMitId(2L)));

        List<Post> ergebnis = service.getFavoritePostsForUser(42L);

        assertThat(ergebnis).extracting(Post::getId).containsExactly(2L, 1L);
    }
}
