package ktb3.full.community.service;

import ktb3.full.community.IntegrationTestSupport;
import ktb3.full.community.config.WithAuthMockUser;
import ktb3.full.community.domain.entity.Post;
import ktb3.full.community.domain.entity.PostLike;
import ktb3.full.community.domain.entity.User;
import ktb3.full.community.fixture.PostFixture;
import ktb3.full.community.fixture.PostLikeFixture;
import ktb3.full.community.fixture.UserFixture;
import ktb3.full.community.repository.PostLikeRepository;
import ktb3.full.community.repository.PostRepository;
import ktb3.full.community.repository.UserRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@WithAuthMockUser
class PostLikeServiceTest extends IntegrationTestSupport {

    @Autowired
    private PostLikeService sut;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostLikeRepository postLikeRepository;

    @Nested
    class createOrUpdate {

        @Test
        void 게시글에_좋아요를_처음_누르는_경우_좋아요_상태는_true가_되고_게시글의_좋아요수가_1_증가한다() {
            // given
            User user = userRepository.save(UserFixture.createUser());
            Post post = postRepository.save(PostFixture.createPost());

            // when
            sut.createOrUpdate(user.getId(), post.getId());

            // then
            List<PostLike> postLikes = postLikeRepository.findAll();
            assertThat(postLikes).hasSize(1)
                    .extracting(PostLike::isLiked)
                    .containsExactly(true);

            Post foundPost = postRepository.findById(post.getId()).orElseThrow();
            assertThat(foundPost.getLikeCount()).isOne();
        }

        @Test
        void 좋아요_상태가_true일때_다시_누르는_경우_좋아요_상태는_false가_되고_게시글의_좋아요수가_1_감소한다() {
            // given
            User user = userRepository.save(UserFixture.createUser());
            Post post = postRepository.save(PostFixture.createWithLikeCount(1));
            PostLike postLike = postLikeRepository.save(PostLikeFixture.createLiked(user, post));

            // when
            sut.createOrUpdate(user.getId(), post.getId());

            // then
            PostLike foundPostLike = postLikeRepository.findById(postLike.getId()).orElseThrow();
            assertThat(foundPostLike.isLiked()).isFalse();

            Post foundPost = postRepository.findById(post.getId()).orElseThrow();
            assertThat(foundPost.getLikeCount()).isZero();
        }

        @Test
        void 좋아요_상태가_false일때_다시_누르는_경우_좋아요_상태는_true가_되고_게시글의_좋아요수가_1_증가한다() {
            // given
            User user = userRepository.save(UserFixture.createUser());
            Post post = postRepository.save(PostFixture.createWithLikeCount(0));
            PostLike postLike = postLikeRepository.save(PostLikeFixture.createNotLiked(user, post));

            // when
            sut.createOrUpdate(user.getId(), post.getId());

            // then
            PostLike foundPostLike = postLikeRepository.findById(postLike.getId()).orElseThrow();
            assertThat(foundPostLike.isLiked()).isTrue();

            Post foundPost = postRepository.findById(post.getId()).orElseThrow();
            assertThat(foundPost.getLikeCount()).isOne();
        }
    }
}