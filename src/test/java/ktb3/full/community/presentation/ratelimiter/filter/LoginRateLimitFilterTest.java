package ktb3.full.community.presentation.ratelimiter.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import ktb3.full.community.IntegrationTestSupport;
import ktb3.full.community.dto.request.UserLoginRequest;
import ktb3.full.community.presentation.ratelimiter.RateLimitResult;
import ktb3.full.community.presentation.ratelimiter.RateLimitType;
import ktb3.full.community.presentation.ratelimiter.RateLimiter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

class LoginRateLimitFilterTest extends IntegrationTestSupport {

    @MockitoBean
    private RateLimiter rateLimiter;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void 로그인_요청이_IP_및_이메일_기준_한도를_모두_초과하지_않으면_요청이_허용된다() throws Exception {
        // given
        UserLoginRequest userLoginRequest = UserLoginRequest.builder()
                .email("email@example.com")
                .password("Password123!")
                .build();

        RateLimitResult allowed = createAllowedResult();

        given(rateLimiter.allowRequest(startsWith("ip:127.0.0.1"), anyLong(), eq(RateLimitType.LOGIN))).willReturn(allowed);
        given(rateLimiter.allowRequest(startsWith("email:email@example.com"), anyLong(), eq(RateLimitType.LOGIN))).willReturn(allowed);

        // when
        ResultActions resultActions = mockMvc.perform(post("/users/login")
                .with(request -> {
                    request.setRemoteAddr("127.0.0.1");
                    return request;
                })
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userLoginRequest)));

        // then
        resultActions
                .andExpect(status().isUnauthorized())
                .andExpect(header().exists("X-RateLimit-Limit"))
                .andExpect(header().exists("X-RateLimit-Remaining"))
                .andExpect(header().exists("X-RateLimit-Reset"))
                .andExpect(jsonPath("$.code").value("4011"))
                .andExpect(jsonPath("$.message").value("아이디 또는 비밀번호가 틀렸습니다."))
                .andExpect(jsonPath("$.data").isEmpty());;
    }

    @Test
    void 로그인_요청이_IP_기준_한도를_초과하면_요청이_차단된다() throws Exception {
        // given
        UserLoginRequest userLoginRequest = UserLoginRequest.builder()
                .email("email@example.com")
                .password("Password123!")
                .build();

        RateLimitResult allowed = createAllowedResult();
        RateLimitResult disallowed = createDisallowedResult();

        given(rateLimiter.allowRequest(startsWith("ip:127.0.0.1"), anyLong(), eq(RateLimitType.LOGIN))).willReturn(disallowed);
        given(rateLimiter.allowRequest(startsWith("email:email@example.com"), anyLong(), eq(RateLimitType.LOGIN))).willReturn(allowed);

        // when
        ResultActions resultActions = mockMvc.perform(post("/users/login")
                .with(request -> {
                    request.setRemoteAddr("127.0.0.1");
                    return request;
                })
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userLoginRequest)));

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

    @Test
    void 로그인_요청이_이메일_기준_한도를_초과하면_요청이_차단된다() throws Exception {
        // given
        UserLoginRequest userLoginRequest = UserLoginRequest.builder()
                .email("email@example.com")
                .password("Password123!")
                .build();

        RateLimitResult allowed = createAllowedResult();
        RateLimitResult disallowed = createDisallowedResult();

        given(rateLimiter.allowRequest(startsWith("ip:127.0.0.1"), anyLong(), eq(RateLimitType.LOGIN))).willReturn(allowed);
        given(rateLimiter.allowRequest(startsWith("email:email@example.com"), anyLong(), eq(RateLimitType.LOGIN))).willReturn(disallowed);

        // when
        ResultActions resultActions = mockMvc.perform(post("/users/login")
                .with(request -> {
                    request.setRemoteAddr("127.0.0.1");
                    return request;
                })
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userLoginRequest)));

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

    private static RateLimitResult createDisallowedResult() {
        return RateLimitResult.builder()
                .consumed(false)
                .build();
    }

    private static RateLimitResult createAllowedResult() {
        return RateLimitResult.builder()
                .consumed(true)
                .build();
    }
}