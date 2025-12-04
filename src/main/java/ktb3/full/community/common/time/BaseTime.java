package ktb3.full.community.common.time;

import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class BaseTime implements Time {

    @Override
    public Instant now() {
        return Instant.now();
    }
}
