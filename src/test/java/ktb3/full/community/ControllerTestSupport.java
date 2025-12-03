package ktb3.full.community;

import com.fasterxml.jackson.databind.ObjectMapper;
import ktb3.full.community.config.TestSecurityConfig;
import ktb3.full.community.presentation.controller.AuthenticatedUserApiController;
import ktb3.full.community.presentation.controller.CommentApiController;
import ktb3.full.community.presentation.controller.PostApiController;
import ktb3.full.community.presentation.controller.UserApiController;
import ktb3.full.community.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@Import({TestSecurityConfig.class})
@ActiveProfiles("test")
@WebMvcTest(controllers = {
        AuthenticatedUserApiController.class,
        UserApiController.class,
        PostApiController.class,
        CommentApiController.class,
})
public abstract class ControllerTestSupport {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockitoBean
    protected UserService userService;

    @MockitoBean
    protected UserDeleteService userDeleteService;

    @MockitoBean
    protected PostService postService;

    @MockitoBean
    protected PostDeleteService postDeleteService;

    @MockitoBean
    protected CommentService commentService;

    @MockitoBean
    protected PostLikeService postLikeService;

    @MockitoBean
    protected ImageUploadService imageUploadService;
}
