package htw.webtech.studyhub.comment;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends CrudRepository<Comment, Long> {

    // Alle Kommentare eines Posts, älteste zuerst (chronologischer Verlauf).
    List<Comment> findByPostIdOrderByCreatedAtAsc(Long postId);

    // Beim Löschen eines Posts alle zugehörigen Kommentare mit entfernen.
    void deleteByPostId(Long postId);
}
