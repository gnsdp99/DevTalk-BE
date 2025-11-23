package ktb3.full.community.service;

import ktb3.full.community.config.IntegrationTest;
import ktb3.full.community.domain.entity.Comment;
import ktb3.full.community.domain.entity.Post;
import ktb3.full.community.domain.entity.User;
import ktb3.full.community.fixture.CommentFixture;
import ktb3.full.community.fixture.PostFixture;
import ktb3.full.community.fixture.UserFixture;
import ktb3.full.community.repository.CommentRepository;
import ktb3.full.community.repository.PostRepository;
import ktb3.full.community.repository.UserRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
class UserDeleteServiceTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserDeleteService userDeleteService;

    @Nested
    class DeleteAccount {

        @Test
        void 회원탈퇴_시_작성한_게시글과_댓글이_전부_익명화된다() {
            // given
            int POST_COUNT = 2;
            int COMMENT_COUNT = 2;

            User user = userRepository.save(UserFixture.createUser());
            List<Post> posts = postRepository.saveAll(PostFixture.createPosts(user, POST_COUNT));
            commentRepository.saveAll(CommentFixture.createComments(user, posts.getFirst(), COMMENT_COUNT));

            // when
            userDeleteService.deleteAccount(user.getId());

            // then
            List<Post> foundPosts = postRepository.findAll();
            List<Comment> foundComments = commentRepository.findAll();

            assertThat(foundPosts)
                    .extracting(Post::getUser)
                    .containsOnlyNulls();

            assertThat(foundComments)
                    .extracting(Comment::getUser)
                    .containsOnlyNulls();
        }
    }
}