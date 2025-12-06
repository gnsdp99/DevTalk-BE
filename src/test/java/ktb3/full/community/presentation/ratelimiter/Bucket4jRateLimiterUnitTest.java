package ktb3.full.community.presentation.ratelimiter;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import ktb3.full.community.UnitTestSupport;
import ktb3.full.community.presentation.ratelimiter.bucket.Bucket4jRateLimiter;
import ktb3.full.community.presentation.ratelimiter.bucket.BucketFactory;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

class Bucket4jRateLimiterUnitTest extends UnitTestSupport {

    @Mock
    private BucketFactory bucketFactory;

    @Nested
    class allowRequest {

        @Test
        void 키가_캐시에_존재하지_않았으면_버킷을_생성해_캐시에_저장한다() {
            // given
            long cacheMaximumSize = 1;

            Bucket bucket = createBucket(null, null, null);
            Cache<String, Bucket> cache = createCache(cacheMaximumSize, null);

            given(bucketFactory.createBucket()).willReturn(bucket);

            // when
            Bucket4jRateLimiter sut = new Bucket4jRateLimiter(cache, bucketFactory);
            sut.allowRequest("newKey", 1);

            // then
            assertThat(cache.getIfPresent("newKey")).isNotNull();
        }

        @Test
        void 캐시가_가득차도_새로운_버킷이_저장된다() {
            // given
            long cacheMaximumSize = 1;

            Bucket bucket = createBucket(null, null, null);
            Cache<String, Bucket> cache = createCache(cacheMaximumSize, null);
            cache.put("oldestKey", bucket);

            given(bucketFactory.createBucket()).willReturn(bucket);

            // when
            Bucket4jRateLimiter sut = new Bucket4jRateLimiter(cache, bucketFactory);
            sut.allowRequest("newKey", 1);

            // then
            assertThat(cache.getIfPresent("newKey")).isNotNull();
        }

        @Test
        void 버킷에_요청수만큼_토큰이_남아있으면_true를_반환한다() {
            // given
            long numTokensToConsume = 1;
            long bucketCapacity = 1;

            Bucket bucket = createBucket(bucketCapacity, null, null);
            Cache<String, Bucket> cache = createCache(null, null);

            given(bucketFactory.createBucket()).willReturn(bucket);

            // when
            Bucket4jRateLimiter sut = new Bucket4jRateLimiter(cache, bucketFactory);
            boolean result = sut.allowRequest("newKey", numTokensToConsume);

            // then
            assertThat(result).isTrue();
        }

        @Test
        void 버킷에_요청수만큼_토큰이_남아있지_않으면_false를_반환한다() {
            // given
            long numTokensToConsume = 2;
            long bucketCapacity = 1;

            Bucket bucket = createBucket(bucketCapacity, null, null);
            Cache<String, Bucket> cache = createCache(null, null);

            given(bucketFactory.createBucket()).willReturn(bucket);

            // when
            Bucket4jRateLimiter sut = new Bucket4jRateLimiter(cache, bucketFactory);
            boolean result = sut.allowRequest("newKey", numTokensToConsume);

            // then
            assertThat(result).isFalse();
        }
    }
    
    private Bucket createBucket(Long capacity, Long refillTokens, Long refillPeriods) {
        long bucketCapacity = capacity == null ? 1 : capacity;
        long bucketRefillTokens = refillTokens == null ? 60000 : refillTokens;
        long bucketRefillPeriods = refillPeriods == null ? 1 : refillPeriods;

        Bandwidth bandwidth = Bandwidth.builder()
                .capacity(bucketCapacity)
                .refillIntervally(bucketRefillTokens, Duration.ofMillis(bucketRefillPeriods))
                .build();

        return Bucket.builder()
                .addLimit(bandwidth)
                .build();
    }

    private Cache<String, Bucket> createCache(Long maximumSize, Long expireAfterAccess) {
        long cacheMaximumSize = maximumSize == null ? 1 : maximumSize;
        long cacheExpireAfterAccess = expireAfterAccess == null ? 60000 : expireAfterAccess;

        return Caffeine.newBuilder()
                .maximumSize(cacheMaximumSize)
                .expireAfterAccess(cacheExpireAfterAccess, TimeUnit.MILLISECONDS)
                .build();
    }
}