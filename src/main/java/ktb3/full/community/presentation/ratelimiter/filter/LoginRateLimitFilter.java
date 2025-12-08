package ktb3.full.community.presentation.ratelimiter.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ktb3.full.community.common.Constants;
import ktb3.full.community.dto.request.UserLoginRequest;
import ktb3.full.community.presentation.ratelimiter.RateLimitResult;
import ktb3.full.community.presentation.ratelimiter.RateLimitType;
import ktb3.full.community.presentation.ratelimiter.RateLimiter;
import ktb3.full.community.presentation.ratelimiter.RateLimiterProperties;
import ktb3.full.community.util.ResponseUtil;
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

    private final RateLimiter rateLimiter;
    private final ObjectMapper objectMapper;
    private final RateLimiterProperties props;

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
        RateLimitResult resultByIpAddr = rateLimiter.allowRequest(clientKeyByIpAddr, props.getNumTokensToConsume(), RateLimitType.LOGIN);
        ResponseUtil.responseRateLimitHeaders(response, resultByIpAddr, props.getLogin());

        if (!resultByIpAddr.isConsumed()) {
            log.info("Login Rate limit exceeded for IP Addr: {}", ipAddr);
            ResponseUtil.responseRateLimitRejected(response, resultByIpAddr, objectMapper);
            return;
        }

        String clientKeyByEmail = "login:email:" + userLoginRequest.getEmail();
        RateLimitResult resultByIpEmail = rateLimiter.allowRequest(clientKeyByEmail, props.getNumTokensToConsume(), RateLimitType.LOGIN);
        ResponseUtil.responseRateLimitHeaders(response, resultByIpEmail, props.getLogin());

        if (!resultByIpEmail.isConsumed()) {
            log.info("Login Rate limit exceeded for Email: {}", userLoginRequest.getEmail());
            ResponseUtil.responseRateLimitRejected(response, resultByIpEmail, objectMapper);
            return;
        }

        filterChain.doFilter(wrappedRequest, response);
    }
}
