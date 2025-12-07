package ktb3.full.community.presentation.ratelimiter.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ktb3.full.community.common.Constants;
import ktb3.full.community.presentation.ratelimiter.RateLimitResult;
import ktb3.full.community.presentation.ratelimiter.RateLimitType;
import ktb3.full.community.presentation.ratelimiter.RateLimiter;
import ktb3.full.community.presentation.ratelimiter.RateLimiterProperties;
import ktb3.full.community.util.ResponseUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@Component
public class UnauthenticatedRateLimitFilter extends OncePerRequestFilter {

    private final RateLimiter rateLimiter;
    private final ObjectMapper objectMapper;
    private final RateLimiterProperties props;

    private final RequestMatcher requestMatcher = PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.GET, Constants.GET_POST_LIST);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken);

        if (!requestMatcher.matches(request) || isAuthenticated) {
            filterChain.doFilter(request, response);
            return;
        }

        String ipAddr = request.getRemoteAddr();
        String clientKey = "ip:" + ipAddr;
        RateLimitResult result = rateLimiter.allowRequest(clientKey, props.getNumTokensToConsume(), RateLimitType.UNAUTHENTICATED);
        ResponseUtil.responseRateLimitHeaders(response, result, props.getUnauthenticated());

        if (!result.isConsumed()) {
            log.info("Rate limit exceeded for IP Addr: {}", ipAddr);
            ResponseUtil.responseRateLimitRejected(response, result, objectMapper);
            return;
        }

        filterChain.doFilter(request, response);
    }
}
