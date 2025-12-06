package ktb3.full.community.presentation.ratelimiter;

import io.github.bucket4j.ConsumptionProbe;
import ktb3.full.community.ControllerTestSupport;
import ktb3.full.community.config.WithAuthMockUser;
import ktb3.full.community.presentation.controller.PostApiController;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import({RateLimiterConfig.class})
@WebMvcTest(controllers = {
        PostApiController.class
})
class RateLimitFilterTest extends ControllerTestSupport {

    @MockitoBean
    private RateLimiter rateLimiter;

    @WithAuthMockUser
    @Test
    void 버킷의_토큰수를_초과하지_않으면_요청이_허용된다() throws Exception {
        // given
        ConsumptionProbe probe = ConsumptionProbe.consumed(1L, 1L);
        given(rateLimiter.allowRequest(anyLong(), anyLong())).willReturn(probe);

        // when
        ResultActions resultActions = mockMvc.perform(get("/posts"));

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(header().exists("X-RateLimit-Limit"))
                .andExpect(header().exists("X-RateLimit-Remaining"))
                .andExpect(header().exists("X-RateLimit-Reset"))
                .andExpect(jsonPath("$.code").isEmpty())
                .andExpect(jsonPath("$.message").value("요청에 성공했습니다."))
                .andExpect(jsonPath("$.data").isEmpty());;
    }

    @WithAuthMockUser
    @Test
    void 버킷의_토큰수를_초과해_요청하면_요청이_거부된다() throws Exception {
        // given
        ConsumptionProbe probe = ConsumptionProbe.rejected(1L, 1L, 1L);
        given(rateLimiter.allowRequest(anyLong(), anyLong())).willReturn(probe);

        // when
        ResultActions resultActions = mockMvc.perform(get("/posts"));

        // then
        resultActions
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("X-RateLimit-Limit"))
                .andExpect(header().exists("X-RateLimit-Remaining"))
                .andExpect(header().exists("X-RateLimit-Reset"))
                .andExpect(header().exists(HttpHeaders.RETRY_AFTER))
                .andExpect(jsonPath("$.code").value(4291))
                .andExpect(jsonPath("$.message").value("요청 한도를 초과했습니다."))
                .andExpect(jsonPath("$.data").isEmpty());;
    }
}