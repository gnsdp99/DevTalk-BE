package ktb3.full.community.repository;

import jakarta.persistence.LockModeType;
import ktb3.full.community.domain.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    @NonNull
    @Query(name = "Post.findByIdActive")
    Optional<Post> findByIdActive(@NonNull @Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(name = "Post.findByIdActive")
    Optional<Post> findByIdForUpdate(@Param("id") Long id);

    @Modifying
    @Query("update Post p set p.viewCount = p.viewCount + 1 where p.id = :id")
    void increaseViewCount(@Param("id") Long id);

    @NonNull
    @Query(value = "select p from Post p left join fetch p.user where p.isDeleted = false",
            countQuery = "select count(p) from Post p where p.isDeleted = false")
    Page<Post> findAllActive(@NonNull Pageable pageable);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = "update Post p set p.user = null where p.user.id = :userId")
    void deleteAllByUserId(@Param("userId") long userId);
}
