package htw.webtech.studyhub.post;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit-Tests für {@link PostFileService}. Repositories und die hochgeladene Datei
 * sind gemockt, dadurch läuft der Test ohne Datenbank und ohne echte Datei.
 */
@ExtendWith(MockitoExtension.class)
class PostFileServiceTest {

    @Mock
    private PostFileRepository fileRepo;
    @Mock
    private PostRepository postRepo;
    @Mock
    private MultipartFile datei;

    @InjectMocks
    private PostFileService service;

    private Post postMitBesitzer(Long id, Long userId) {
        Post post = new Post();
        post.setId(id);
        post.setUserId(userId);
        return post;
    }

    @Test
    void store_alsBesitzer_speichertDateiUndSetztDateinamen() throws Exception {
        Post post = postMitBesitzer(1L, 42L);
        when(postRepo.findById(1L)).thenReturn(Optional.of(post));
        when(datei.isEmpty()).thenReturn(false);
        when(datei.getSize()).thenReturn(1024L);
        when(datei.getContentType()).thenReturn("application/pdf");
        when(datei.getOriginalFilename()).thenReturn("skript.pdf");
        when(datei.getBytes()).thenReturn(new byte[]{1, 2, 3});
        when(fileRepo.findByPostId(1L)).thenReturn(Optional.empty());

        service.store(1L, datei, 42L);

        verify(fileRepo).save(any(PostFile.class));
        // Der Dateiname wird am Post hinterlegt, damit das Frontend den Download kennt.
        assertThat(post.getFileName()).isEqualTo("skript.pdf");
        verify(postRepo).save(post);
    }

    @Test
    void store_fremderUser_wirft403() {
        Post post = postMitBesitzer(1L, 42L);
        when(postRepo.findById(1L)).thenReturn(Optional.of(post));

        assertThatThrownBy(() -> service.store(1L, datei, 7L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("statusCode").isEqualTo(HttpStatus.FORBIDDEN);

        verify(fileRepo, never()).save(any(PostFile.class));
    }

    @Test
    void store_unbekannterPost_wirft404() {
        when(postRepo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.store(99L, datei, 42L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("statusCode").isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void store_ungueltigerDateityp_wirft400() {
        Post post = postMitBesitzer(1L, 42L);
        when(postRepo.findById(1L)).thenReturn(Optional.of(post));
        when(datei.isEmpty()).thenReturn(false);
        when(datei.getSize()).thenReturn(1024L);
        when(datei.getContentType()).thenReturn("text/plain");

        assertThatThrownBy(() -> service.store(1L, datei, 42L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("statusCode").isEqualTo(HttpStatus.BAD_REQUEST);

        verify(fileRepo, never()).save(any(PostFile.class));
    }

    @Test
    void store_zuGrosseDatei_wirft400() {
        Post post = postMitBesitzer(1L, 42L);
        when(postRepo.findById(1L)).thenReturn(Optional.of(post));
        when(datei.isEmpty()).thenReturn(false);
        // Über dem Limit von 5 MB
        when(datei.getSize()).thenReturn(6L * 1024 * 1024);

        assertThatThrownBy(() -> service.store(1L, datei, 42L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("statusCode").isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void get_vorhandeneDatei_wirdZurueckgegeben() {
        PostFile file = new PostFile();
        file.setPostId(1L);
        when(fileRepo.findByPostId(1L)).thenReturn(Optional.of(file));

        assertThat(service.get(1L)).isSameAs(file);
    }

    @Test
    void get_ohneDatei_wirft404() {
        when(fileRepo.findByPostId(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(1L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("statusCode").isEqualTo(HttpStatus.NOT_FOUND);
    }
}
