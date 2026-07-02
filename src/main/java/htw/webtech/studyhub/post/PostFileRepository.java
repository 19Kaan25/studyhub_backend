package htw.webtech.studyhub.post;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostFileRepository extends CrudRepository<PostFile, Long> {

    Optional<PostFile> findByPostId(Long postId);
}
