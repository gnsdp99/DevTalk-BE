package ktb3.full.community.presentation.ratelimiter;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "rate-limiter")
public class RateLimiterProperties {

    private final long numTokensToConsume;

    private final PolicyProperties login;
    private final PolicyProperties authenticated;
    private final PolicyProperties unauthenticated;

    @Getter
    @RequiredArgsConstructor
    public static class PolicyProperties {
        private final BucketProperties bucket;
        private final CacheProperties cache;
    }

    @Getter
    @RequiredArgsConstructor
    public static class BucketProperties {
        private final long capacity;
        private final long refillTokens;
        private final long refillPeriods;
    }

    @Getter
    @RequiredArgsConstructor
    public static class CacheProperties {
        private final long maximumSize;
        private final long expireAfterAccess;
    }

    public RateLimiterProperties.PolicyProperties getPolicyProps(RateLimitType type) {
        return switch (type) {
            case LOGIN -> getLogin();
            case AUTHENTICATED -> getAuthenticated();
            case UNAUTHENTICATED -> getUnauthenticated();
        };
    }
}
