package ktb3.full.community.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import ktb3.full.community.IntegrationTestSupport;
import ktb3.full.community.config.WithAuthMockUser;
import ktb3.full.community.domain.entity.Comment;
import ktb3.full.community.domain.entity.Post;
import ktb3.full.community.domain.entity.User;
import ktb3.full.community.dto.request.CommentUpdateRequest;
import ktb3.full.community.fixture.CommentFixture;
import ktb3.full.community.fixture.PostFixture;
import ktb3.full.community.fixture.UserFixture;
import ktb3.full.community.repository.CommentRepository;
import ktb3.full.community.repository.PostRepository;
import ktb3.full.community.repository.UserRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ResourceOwnerAuthorizationTest extends IntegrationTestSupport {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Nested
    class resource_owner {

        @WithAuthMockUser(userId = 1L)
        @Test
        void 리소스_소유자인_경우_200을_응답한다() throws Exception {
            // given
            User author = userRepository.save(UserFixture.createUser("author@example.com", "author"));
            userRepository.save(UserFixture.createUser("user@example.com", "user"));
            Post post = postRepository.save(PostFixture.createPost(author));
            Comment comment = commentRepository.save(CommentFixture.createComment(author, post, "content"));

            // when
            CommentUpdateRequest request = new CommentUpdateRequest("updated content");

            ResultActions resultActions = mockMvc.perform(patch("/comments/{commentId}", comment.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .with(csrf()));

            // then
            resultActions
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").isEmpty())
                    .andExpect(jsonPath("$.message").value("요청에 성공했습니다."))
                    .andExpect(jsonPath("$.data").isEmpty());
        }

        @WithAuthMockUser(userId = 2L)
        @Test
        void 리소스_소유자가_아닌_경우_403을_응답한다() throws Exception {
            // given
            User author = userRepository.save(UserFixture.createUser("author@example.com", "author"));
            userRepository.save(UserFixture.createUser("user@example.com", "user"));
            Post post = postRepository.save(PostFixture.createPost(author));
            Comment comment = commentRepository.save(CommentFixture.createComment(author, post, "content"));

            CommentUpdateRequest request = new CommentUpdateRequest("updated content");

            // when
            ResultActions resultActions = mockMvc.perform(patch("/comments/{commentId}", comment.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .with(csrf()));

            // then
            resultActions
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("4031"))
                    .andExpect(jsonPath("$.message").value("접근 권한이 존재하지 않습니다."))
                    .andExpect(jsonPath("$.data").isEmpty());
        }
    }
}

