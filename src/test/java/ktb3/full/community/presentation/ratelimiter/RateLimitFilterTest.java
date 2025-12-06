package ktb3.full.community.presentation.ratelimiter;

import ktb3.full.community.ControllerTestSupport;
import ktb3.full.community.presentation.controller.PostApiController;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import({RateLimiterConfig.class})
@WebMvcTest(controllers = {
        PostApiController.class
})
class RateLimitFilterTest extends ControllerTestSupport {

    @MockitoBean
    private RateLimiter rateLimiter;

    @Test
    void 버킷의_토큰수를_초과하지_않으면_요청이_허용된다() throws Exception {
        // given
        given(rateLimiter.allowRequest(any(String.class), any(Long.class))).willReturn(true);

        // when
        ResultActions resultActions = mockMvc.perform(get("/posts"));

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").isEmpty())
                .andExpect(jsonPath("$.message").value("요청에 성공했습니다."))
                .andExpect(jsonPath("$.data").isEmpty());;
    }

    @Test
    void 버킷의_토큰수를_초과해_요청하면_요청이_거부된다() throws Exception {
        // given
        given(rateLimiter.allowRequest(anyString(), anyLong())).willReturn(false);

        // when
        ResultActions resultActions = mockMvc.perform(get("/posts"));

        // then
        resultActions
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code").value(4291))
                .andExpect(jsonPath("$.message").value("요청 한도를 초과했습니다."))
                .andExpect(jsonPath("$.data").isEmpty());;
    }
}