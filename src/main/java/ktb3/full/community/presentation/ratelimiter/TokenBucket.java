package ktb3.full.community.presentation.ratelimiter;

import ktb3.full.community.common.time.Time;
import lombok.Builder;

import java.util.concurrent.atomic.AtomicLong;

public class TokenBucket {

    private final long capacity; // 최대 토큰 수
    private final long refillTokens; // 초당 리필하는 토큰 수
    private final long refillInterval; // 리필 간격 (millis)
    private final AtomicLong tokens; // 남은 토큰 수
    private final AtomicLong lastRefillTimestamp; // 마지막 리필 시간
    private final Time time;

    public static TokenBucket create(long capacity, long refillTokens, long refillInterval, Time time) {
        return TokenBucket.builder()
                .capacity(capacity)
                .refillTokens(refillTokens)
                .refillInterval(refillInterval)
                .time(time)
                .build();
    }

    @Builder
    private TokenBucket(long capacity, long refillTokens, long refillInterval, Time time) {
        this.capacity = capacity;
        this.refillTokens = refillTokens;
        this.refillInterval = refillInterval;
        this.tokens = new AtomicLong(capacity);
        this.lastRefillTimestamp = new AtomicLong(time.now().toEpochMilli());
        this.time = time;
    }

    public long getTokens() {
        return tokens.get();
    }

    public synchronized boolean tryConsume() {
        refill();

        if (tokens.get() > 0) {
            tokens.decrementAndGet();
            return true;
        }

        return false;
    }

    private void refill() {
        long now = time.now().toEpochMilli();
        long lastRefill = lastRefillTimestamp.get();
        long intervals = (now - lastRefill) / refillInterval;

        if (intervals > 0) {
            long newTokens = Math.min(capacity, tokens.get() + intervals * refillTokens);
            tokens.set(newTokens);
            lastRefillTimestamp.set(now);
        }
    }
}
