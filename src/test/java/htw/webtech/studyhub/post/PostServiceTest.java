package htw.webtech.studyhub.post;

import htw.webtech.studyhub.comment.CommentRepository;
import htw.webtech.studyhub.favorite.FavoriteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit-Tests für die {@link PostService}-Methoden. Die Repositories sind gemockt,
 * dadurch laufen die Tests ohne Datenbank und ohne Spring-Kontext.
 */
@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository repo;
    @Mock
    private PostFileRepository fileRepo;
    @Mock
    private CommentRepository commentRepo;
    @Mock
    private FavoriteRepository favoriteRepo;

    @InjectMocks
    private PostService service;

    /** Hilfsmethode: erzeugt einen Post mit Id und Besitzer. */
    private Post postMitBesitzer(Long id, Long userId) {
        Post post = new Post();
        post.setId(id);
        post.setUserId(userId);
        post.setTitle("Alter Titel");
        return post;
    }

    @Test
    void get_vorhandenerPost_wirdZurueckgegeben() {
        Post post = postMitBesitzer(1L, 42L);
        when(repo.findById(1L)).thenReturn(Optional.of(post));

        assertThat(service.get(1L)).isSameAs(post);
    }

    @Test
    void get_unbekannteId_wirft404() {
        when(repo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(99L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("statusCode").isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void create_setztUserIdAusToken() {
        Post eingang = new Post();
        eingang.setTitle("Neuer Post");
        // userId aus dem Body darf nicht durchschlagen
        eingang.setUserId(999L);
        when(repo.save(any(Post.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Post gespeichert = service.create(eingang, 42L);

        assertThat(gespeichert.getUserId()).isEqualTo(42L);
    }

    @Test
    void update_alsBesitzer_aendertFelder() {
        Post bestehend = postMitBesitzer(1L, 42L);
        when(repo.findById(1L)).thenReturn(Optional.of(bestehend));
        when(repo.save(any(Post.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Post neueDaten = new Post();
        neueDaten.setTitle("Neuer Titel");
        neueDaten.setContent("Neuer Inhalt");
        neueDaten.setType(PostType.LINK);

        Post ergebnis = service.update(1L, neueDaten, 42L);

        assertThat(ergebnis.getTitle()).isEqualTo("Neuer Titel");
        assertThat(ergebnis.getContent()).isEqualTo("Neuer Inhalt");
        assertThat(ergebnis.getType()).isEqualTo(PostType.LINK);
    }

    @Test
    void update_alsFremderUser_wirft403() {
        Post bestehend = postMitBesitzer(1L, 42L);
        when(repo.findById(1L)).thenReturn(Optional.of(bestehend));

        assertThatThrownBy(() -> service.update(1L, new Post(), 7L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("statusCode").isEqualTo(HttpStatus.FORBIDDEN);

        verify(repo, never()).save(any(Post.class));
    }

    @Test
    void delete_alsBesitzer_loeschtPostUndAbhaengigeDaten() {
        Post bestehend = postMitBesitzer(1L, 42L);
        when(repo.findById(1L)).thenReturn(Optional.of(bestehend));
        when(fileRepo.findByPostId(1L)).thenReturn(Optional.empty());

        service.delete(1L, 42L);

        verify(commentRepo).deleteByPostId(1L);
        verify(favoriteRepo).deleteByPostId(1L);
        verify(repo).delete(bestehend);
    }

    @Test
    void delete_alsFremderUser_wirft403() {
        Post bestehend = postMitBesitzer(1L, 42L);
        when(repo.findById(1L)).thenReturn(Optional.of(bestehend));

        assertThatThrownBy(() -> service.delete(1L, 7L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("statusCode").isEqualTo(HttpStatus.FORBIDDEN);

        verify(repo, never()).delete(any(Post.class));
    }
}
