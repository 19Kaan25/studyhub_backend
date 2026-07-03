package htw.webtech.studyhub.favorite;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends CrudRepository<Favorite, Long> {

    boolean existsByUserIdAndPostId(Long userId, Long postId);

    Optional<Favorite> findByUserIdAndPostId(Long userId, Long postId);

    // Favoriten eines Users, zuletzt markierte zuerst.
    List<Favorite> findByUserIdOrderByCreatedAtDesc(Long userId);

    // Beim Löschen eines Posts alle Favoriten-Markierungen dazu entfernen.
    void deleteByPostId(Long postId);
}
