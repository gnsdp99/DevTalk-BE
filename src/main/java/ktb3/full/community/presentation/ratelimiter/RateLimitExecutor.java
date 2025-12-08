package ktb3.full.community.presentation.ratelimiter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import ktb3.full.community.util.ResponseUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@Component
public class RateLimitExecutor {

    private final RateLimiter rateLimiter;
    private final RateLimiterProperties props;
    private final ObjectMapper objectMapper;

    public boolean execute(String clientKey, RateLimitType type, String logMessage, HttpServletResponse response) throws IOException {
        RateLimiterProperties.PolicyProperties policyProps = props.getPolicyProps(type);

        RateLimitResult result = rateLimiter.allowRequest(clientKey, props.getNumTokensToConsume(), type);
        ResponseUtil.responseRateLimitHeaders(response, result, policyProps);

        if (!result.isConsumed()) {
            log.info(logMessage);
            ResponseUtil.responseRateLimitRejected(response, result, objectMapper);
            return false;
        }

        return true;
    }
}
