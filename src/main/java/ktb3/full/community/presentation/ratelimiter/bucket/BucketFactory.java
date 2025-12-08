package ktb3.full.community.presentation.ratelimiter.bucket;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.BandwidthBuilder;
import io.github.bucket4j.Bucket;
import ktb3.full.community.presentation.ratelimiter.RateLimitType;
import ktb3.full.community.presentation.ratelimiter.RateLimiterProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;

@RequiredArgsConstructor
@Component
public class BucketFactory {

    private final RateLimiterProperties properties;

    public Bucket createBucket(RateLimitType rateLimitType) {
        return Bucket.builder()
                .addLimit(createBandwidth(rateLimitType))
                .build();
    }

    private Bandwidth createBandwidth(RateLimitType type) {
        RateLimiterProperties.BucketProperties props = properties.getPolicyProps(type).getBucket();
        Duration period = Duration.ofSeconds(props.getRefillPeriods());

        BandwidthBuilder.BandwidthBuilderRefillStage bandWithBuilder = Bandwidth.builder()
                .capacity(props.getCapacity());

        return switch (type) {
            case LOGIN -> bandWithBuilder
                    .refillIntervally(props.getRefillTokens(), period)
                    .build();

            case AUTHENTICATED, UNAUTHENTICATED -> bandWithBuilder
                    .refillGreedy(props.getRefillTokens(), period)
                    .build();
        };
    }
}
