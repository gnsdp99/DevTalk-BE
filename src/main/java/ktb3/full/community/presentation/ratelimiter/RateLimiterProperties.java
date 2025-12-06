package ktb3.full.community.presentation.ratelimiter;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "rate-limiter")
public class RateLimiterProperties {

    private final CacheProperties cache;
    private final BucketProperties bucket;
    private final Long numTokensToConsume;

    @Getter
    @RequiredArgsConstructor
    public static class CacheProperties {
        private final long maximumSize;
        private final long expireAfterAccess;
    }

    @Getter
    @RequiredArgsConstructor
    public static class BucketProperties {
        private final long capacity;
        private final long refillTokens;
        private final long refillPeriods;
    }
}
