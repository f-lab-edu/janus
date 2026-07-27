package kr.ai.janus.parsing.scan;

import java.time.LocalDateTime;

/**
 * 메시지 시각을 하나씩 받아 대화 기간(가장 이른 시각 ~ 가장 늦은 시각)을 추적한다.
 * 1차, 2차 스캔이 공유한다.
 */
final class MessagePeriodTracker {

    private LocalDateTime startedAt;
    private LocalDateTime endedAt;

    void updateWith(LocalDateTime sentAt) {
        updateStartedAt(sentAt);
        updateEndedAt(sentAt);
    }

    private void updateStartedAt(LocalDateTime sentAt) {
        if (startedAt == null || sentAt.isBefore(startedAt)) {
            startedAt = sentAt;
        }
    }

    private void updateEndedAt(LocalDateTime sentAt) {
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
