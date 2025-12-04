package ktb3.full.community.stub;

import ktb3.full.community.common.time.Time;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class StubTime implements Time {

    private final Instant currentTime;

    public StubTime(LocalDateTime localDateTime) {
        ZoneId zone = ZoneId.of("Asia/Seoul");
        this.currentTime = localDateTime.atZone(zone).toInstant();
    }

    @Override
    public Instant now() {
        return currentTime;
    }
}
