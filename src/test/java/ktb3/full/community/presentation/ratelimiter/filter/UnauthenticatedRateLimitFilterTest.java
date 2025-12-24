package ktb3.full.community.presentation.ratelimiter.filter;

import ktb3.full.community.IntegrationTestSupport;
import ktb3.full.community.fixture.RateLimitResultFixture;
import ktb3.full.community.presentation.ratelimiter.RateLimitResult;
import ktb3.full.community.presentation.ratelimiter.RateLimitType;
import ktb3.full.community.presentation.ratelimiter.RateLimiter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

class UnauthenticatedRateLimitFilterTest extends IntegrationTestSupport {

    @MockitoBean
    private RateLimiter rateLimiter;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void 미인증_사용자의_요청이_IP_기준_한도를_초과하지_않으면_요청이_허용된다() throws Exception {
        // given
        RateLimitResult allowed = RateLimitResultFixture.createAllowedResult();

        given(rateLimiter.allowRequest(startsWith("ip:127.0.0.1"), anyLong(), eq(RateLimitType.UNAUTHENTICATED))).willReturn(allowed);

        // when
        ResultActions resultActions = mockMvc.perform(get("/api/posts")
                .with(request -> {
                    request.setRemoteAddr("127.0.0.1");
                    return request;
                }));

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(header().exists("X-RateLimit-Limit"))
                .andExpect(header().exists("X-RateLimit-Remaining"))
                .andExpect(header().exists("X-RateLimit-Reset"))
                .andExpect(jsonPath("$.code").isEmpty())
                .andExpect(jsonPath("$.message").value("요청에 성공했습니다."))
                .andExpect(jsonPath("$.data").isNotEmpty());
    }

    @Test
    void 미인증_사용자의_요청이_IP_기준_한도를_초과하면_요청이_차단된다() throws Exception {
        // given
        RateLimitResult disallowed = RateLimitResultFixture.createDisallowedResult();

        given(rateLimiter.allowRequest(startsWith("ip:127.0.0.1"), anyLong(), eq(RateLimitType.UNAUTHENTICATED))).willReturn(disallowed);

        // when
        ResultActions resultActions = mockMvc.perform(get("/api/posts")
                .with(request -> {
                    request.setRemoteAddr("127.0.0.1");
                    return request;
                }));

        // then
        resultActions
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("X-RateLimit-Limit"))
                .andExpect(header().exists("X-RateLimit-Remaining"))
                .andExpect(header().exists("X-RateLimit-Reset"))
                .andExpect(header().exists(HttpHeaders.RETRY_AFTER))
                .andExpect(jsonPath("$.code").value("4291"))
                .andExpect(jsonPath("$.message").value("요청 한도를 초과했습니다."))
                .andExpect(jsonPath("$.data").isEmpty());;
    }
}