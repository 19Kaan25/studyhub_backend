package htw.webtech.studyhub.comment;

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
 * Unit-Tests für {@link CommentService}. Die Repositories sind gemockt,
 * dadurch läuft der Test ohne Datenbank.
 */
@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository repo;
    @Mock
    private PostRepository postRepo;

    @InjectMocks
    private CommentService service;

    private Comment kommentarVon(Long id, Long userId) {
        Comment comment = new Comment();
        comment.setId(id);
        comment.setUserId(userId);
        comment.setContent("Ein Kommentar");
        return comment;
    }

    @Test
    void getForPost_liefertKommentareDesPosts() {
        List<Comment> kommentare = List.of(kommentarVon(1L, 10L));
        when(repo.findByPostIdOrderByCreatedAtAsc(5L)).thenReturn(kommentare);

        assertThat(service.getForPost(5L)).isEqualTo(kommentare);
    }

    @Test
    void create_beiVorhandenemPost_speichertKommentar() {
        when(postRepo.existsById(5L)).thenReturn(true);
        when(repo.save(any(Comment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Comment gespeichert = service.create(5L, "Sehr hilfreich", 42L, "max");

        assertThat(gespeichert.getPostId()).isEqualTo(5L);
        assertThat(gespeichert.getUserId()).isEqualTo(42L);
        assertThat(gespeichert.getAuthorUsername()).isEqualTo("max");
        assertThat(gespeichert.getContent()).isEqualTo("Sehr hilfreich");
    }

    @Test
    void create_beiUnbekanntemPost_wirft404() {
        when(postRepo.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> service.create(99L, "Text", 42L, "max"))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("statusCode").isEqualTo(HttpStatus.NOT_FOUND);

        verify(repo, never()).save(any(Comment.class));
    }

    @Test
    void delete_alsAutor_loeschtKommentar() {
        Comment comment = kommentarVon(1L, 42L);
        when(repo.findById(1L)).thenReturn(Optional.of(comment));

        service.delete(1L, 42L);

        verify(repo).delete(comment);
    }

    @Test
    void delete_alsFremderUser_wirft403() {
        Comment comment = kommentarVon(1L, 42L);
        when(repo.findById(1L)).thenReturn(Optional.of(comment));

        assertThatThrownBy(() -> service.delete(1L, 7L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("statusCode").isEqualTo(HttpStatus.FORBIDDEN);

        verify(repo, never()).delete(any(Comment.class));
    }

    @Test
    void delete_unbekannterKommentar_wirft404() {
        when(repo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(99L, 42L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("statusCode").isEqualTo(HttpStatus.NOT_FOUND);
    }
}
