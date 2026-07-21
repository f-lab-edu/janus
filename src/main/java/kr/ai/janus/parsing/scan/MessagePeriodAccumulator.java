package kr.ai.janus.parsing.scan;

import java.time.LocalDateTime;

final class MessagePeriodAccumulator {

    private LocalDateTime startedAt;
    private LocalDateTime endedAt;

    void accumulate(LocalDateTime sentAt) {
        if (startedAt == null || sentAt.isBefore(startedAt)) {
            startedAt = sentAt;
        }
        if (endedAt == null || sentAt.isAfter(endedAt)) {
            endedAt = sentAt;
        }
    }

    LocalDateTime startedAt() {
        return startedAt;
    }

    LocalDateTime endedAt() {
        return endedAt;
    }
}
