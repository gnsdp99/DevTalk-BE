package ktb3.full.community.service;

import ktb3.full.community.IntegrationTestSupport;
import ktb3.full.community.config.WithAuthMockUser;
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

class PostDeleteServiceTest extends IntegrationTestSupport {

    @Autowired
    private PostDeleteService sut;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Nested
    class deletePost {

        @WithAuthMockUser
        @Test
        void 게시글을_삭제하면_작성된_댓글이_모두_soft_delete된다() {
            // given
            int numComments = 3;
            User user = userRepository.save(UserFixture.createUser());
            Post post = postRepository.save(PostFixture.createPost(user));
            commentRepository.saveAll(CommentFixture.createComments(user, post, numComments));

            // when
            sut.deletePost(post.getId());

            // then
            List<Comment> comments = commentRepository.findAll();
            assertThat(comments).hasSize(numComments)
                    .extracting(Comment::isDeleted)
                    .containsOnly(true);
        }
    }
}