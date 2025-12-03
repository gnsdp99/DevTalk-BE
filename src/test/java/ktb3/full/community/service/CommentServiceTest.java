package ktb3.full.community.service;

import ktb3.full.community.IntegrationTestSupport;
import ktb3.full.community.config.WithAuthMockUser;
import ktb3.full.community.domain.entity.Comment;
import ktb3.full.community.domain.entity.Post;
import ktb3.full.community.domain.entity.User;
import ktb3.full.community.dto.request.CommentCreateRequest;
import ktb3.full.community.dto.request.CommentUpdateRequest;
import ktb3.full.community.dto.response.CommentResponse;
import ktb3.full.community.fixture.CommentFixture;
import ktb3.full.community.fixture.PostFixture;
import ktb3.full.community.fixture.UserFixture;
import ktb3.full.community.repository.CommentRepository;
import ktb3.full.community.repository.PostRepository;
import ktb3.full.community.repository.UserRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

class CommentServiceTest extends IntegrationTestSupport {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private CommentService sut;

    @Nested
    class createComment {

        @Test
        void 댓글을_생성한다() {
            // given
            User user = userRepository.save(UserFixture.createUser());
            Post post = postRepository.save(PostFixture.createPost(user));

            CommentCreateRequest request = CommentCreateRequest.builder()
                    .content("content")
                    .build();

            // when
            CommentResponse response = sut.createComment(user.getId(), post.getId(), request);

            // then
            assertThat(response.getCommentId()).isNotNull();
            assertThat(response.getContent()).isEqualTo("content");
        }
    }

    @Nested
    class updateComment {

        @WithAuthMockUser
        @Test
        void 댓글의_내용을_수정한다() {
            // given
            User user = userRepository.save(UserFixture.createUser());
            Post post = postRepository.save(PostFixture.createPost(user));
            Comment comment = commentRepository.save(CommentFixture.createComment(user, post, "content"));

            CommentUpdateRequest request = CommentUpdateRequest.builder()
                    .content("updated comment")
                    .build();

            // when
            sut.updateComment(comment.getId(), request);

            // then
            Comment foundComment = commentRepository.findById(comment.getId()).orElseThrow();
            assertThat(foundComment.getContent()).isEqualTo("updated comment");
        }
    }

    @Nested
    class deleteComment {

        @WithAuthMockUser
        @Test
        void 댓글을_삭제하면_게시글의_댓글수가_1_감소한다() {
            // given
            User user = userRepository.save(UserFixture.createUser());
            Post post = postRepository.save(PostFixture.createWithCommentCount(user, 1));
            Comment comment = commentRepository.save(CommentFixture.createComment(user, post, "content"));

            // when
            sut.deleteComment(comment.getId());

            // then
            Post foundPost = postRepository.findById(post.getId()).orElseThrow();
            assertThat(foundPost.getCommentCount()).isZero();
        }
    }
}