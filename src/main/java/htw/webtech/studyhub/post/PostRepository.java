package htw.webtech.studyhub.post;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends CrudRepository<Post, Long> {

    // Alle Posts eines Users, neueste zuerst.
    List<Post> findByUserIdOrderByCreatedAtDesc(Long userId);
}
