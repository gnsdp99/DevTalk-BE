package ktb3.full.community.repository;

import jakarta.persistence.EntityManager;
import ktb3.full.community.RepositoryTestSupport;
import ktb3.full.community.domain.entity.Post;
import ktb3.full.community.domain.entity.User;
import ktb3.full.community.fixture.PostFixture;
import ktb3.full.community.fixture.UserFixture;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PostRepositoryTest extends RepositoryTestSupport {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Nested
    class findByIdActive {

        @Test
        void 활성화된_게시글이면_조회된다() {
            // given
            User user = userRepository.save(UserFixture.createUser());
            Post post = postRepository.save(PostFixture.createPost(user));

            // when
            Post foundPost = postRepository.findByIdActive(post.getId()).orElse(null);

            // then
            assertThat(foundPost).isNotNull();
        }

        @Test
        void 삭제된_게시글이면_조회되지_않는다() {
            // given
            User user = userRepository.save(UserFixture.createUser());
            Post post = postRepository.save(PostFixture.createDeleted(user));

            // when
            Post foundPost = postRepository.findByIdActive(post.getId()).orElse(null);

            // then
            assertThat(foundPost).isNull();
        }
    }

    @Nested
    class increaseViewCount {

        @Test
        void 조회수를_1_증가한다() {
            // given
            User user = userRepository.save(UserFixture.createUser());
            Post post = postRepository.save(PostFixture.createWithViewCount(user, 0));
            entityManager.clear();

            // when
            postRepository.increaseViewCount(post.getId());

            // then
            Post foundPost = postRepository.findById(post.getId()).orElseThrow();

            assertThat(foundPost.getViewCount()).isEqualTo(1);
        }
    }

    @Nested
    class findAllActive {

        @Test
        void 활성화된_게시글만_조회한다() {
            // given
            int activePostCount = 5;
            int deletedPostCount = 10;
            User user = userRepository.save(UserFixture.createUser());
            postRepository.saveAll(PostFixture.createPosts(user, activePostCount));
            postRepository.saveAll(PostFixture.createDeletedPosts(user, deletedPostCount));

            // when
            int pageNumber = 0;
            int pageSize = 10;
            PageRequest request = PageRequest.of(pageNumber, pageSize);

            Page<Post> foundPosts = postRepository.findAllActive(request);

            // then
            assertThat(foundPosts.getNumberOfElements()).isEqualTo(5);
            assertThat(foundPosts.getContent())
                    .extracting(Post::isDeleted)
                    .containsOnly(false);
        }

        @Test
        void 작성자가_없는_게시글도_조회한다() {
            // given
            int withUserPostCount = 5;
            int withoutUserPostCount = 5;
            User user = userRepository.save(UserFixture.createUser());
            postRepository.saveAll(PostFixture.createPosts(user, withUserPostCount));
            postRepository.saveAll(PostFixture.createWithoutUserPosts(withoutUserPostCount));

            // when
            int pageNumber = 0;
            int pageSize = 10;
            PageRequest request = PageRequest.of(pageNumber, pageSize);

            Page<Post> page = postRepository.findAllActive(request);

            // then
            assertThat(page.getNumberOfElements()).isEqualTo(10);
        }
    }

    @Nested
    class deleteAllByUserId {

        @Test
        void 게시글의_작성자를_삭제한다() {
            // given
            int postCount = 10;
            User user = userRepository.save(UserFixture.createUser());
            postRepository.saveAll(PostFixture.createPosts(user, postCount));

            // when
            postRepository.deleteAllByUserId(user.getId());

            // then
            List<Post> foundPosts = postRepository.findAll();

            assertThat(foundPosts)
                    .extracting(Post::getUser)
                    .containsOnlyNulls();
        }
    }
}