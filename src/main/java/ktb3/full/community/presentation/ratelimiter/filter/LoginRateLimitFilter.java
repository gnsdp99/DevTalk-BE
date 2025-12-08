package ktb3.full.community.presentation.ratelimiter.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ktb3.full.community.common.Constants;
import ktb3.full.community.dto.request.UserLoginRequest;
import ktb3.full.community.presentation.ratelimiter.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@Component
public class LoginRateLimitFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;
    private final RateLimitExecutor executor;

    private final RequestMatcher loginRequestMatcher = PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.POST, Constants.LOGIN);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        if (!loginRequestMatcher.matches(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);

        String ipAddr = wrappedRequest.getRemoteAddr();
        UserLoginRequest userLoginRequest = objectMapper.readValue(wrappedRequest.getInputStream(), UserLoginRequest.class);

        String clientKeyByIpAddr = "login:ip:" + ipAddr;
        String logMessageByIpAddr = "Login Rate limit exceeded for IP Addr: " + ipAddr;

        if (!executor.execute(
                clientKeyByIpAddr,
                RateLimitType.LOGIN,
                logMessageByIpAddr,
                response)
        ) {
            return;
        }

        String clientKeyByEmail = "login:email:" + userLoginRequest.getEmail();
        String logMessageByEmail = "Login Rate limit exceeded for Email: " + userLoginRequest.getEmail();

        if (!executor.execute(
                clientKeyByEmail,
                RateLimitType.LOGIN,
                logMessageByEmail,
                response)
        ) {
            return;
        }

        filterChain.doFilter(wrappedRequest, response);
    }
}
