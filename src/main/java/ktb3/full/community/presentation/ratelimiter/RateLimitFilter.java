package ktb3.full.community.presentation.ratelimiter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ktb3.full.community.common.exception.ApiErrorCode;
import ktb3.full.community.dto.response.ApiResponse;
import ktb3.full.community.security.userdetails.AuthUserDetails;
import ktb3.full.community.util.ResponseUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimiter rateLimiter;
    private final ObjectMapper objectMapper;
    private final RateLimiterProperties props;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null) {
            AuthUserDetails principal = (AuthUserDetails) authentication.getPrincipal();
            Long target = principal.getUserId();

            if (isRequestRejected(target, response)) {
                log.info("Rate limit exceeded for userId = {}", target);
                return;
            }
        }

        if (authentication == null) {
            Object target = request.getRemoteAddr();

            if (isRequestRejected(target, response)) {
                log.info("Rate limit exceeded for IP Addr: {}", target);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isRequestRejected(Object target, HttpServletResponse response) throws IOException {
        if (!rateLimiter.allowRequest(target.toString(), props.getNumTokensToConsume())) {
            ApiResponse<Void> apiResponse = ApiResponse.error(ApiErrorCode.TOO_MANY_REQUESTS);
            ResponseUtil.responseJsonUtf8(response, HttpStatus.TOO_MANY_REQUESTS.value(), objectMapper.writeValueAsString(apiResponse));
            return true;
        }

        return false;
    }
}
