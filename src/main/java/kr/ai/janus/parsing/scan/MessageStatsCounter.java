package kr.ai.janus.parsing.scan;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import kr.ai.janus.parsing.model.MessageType;
import kr.ai.janus.parsing.model.ParseStats;
import kr.ai.janus.parsing.model.RawMessage;
import kr.ai.janus.parsing.model.SpeakerRole;

/**
 * 2차 스캔에서 메시지를 하나씩 받아 통계를 세고, 마지막에 ParseStats로 낸다..
 */
final class MessageStatsCounter {

    private final String ownerName;

    private long ownerCount;
    private long partnerCount;
    private long textCount;
    private long emoticonCount;
    private long photoCount;
    private final MessagePeriodTracker period = new MessagePeriodTracker();
    private final Set<LocalDate> activeDays = new HashSet<>();

    MessageStatsCounter(String ownerName) {
        this.ownerName = ownerName;
    }

    void add(RawMessage message) {
        countRole(message.speakerName());
        countType(message.text());
        period.updateWith(message.sentAt());
        activeDays.add(message.sentAt().toLocalDate());
    }

    private void countRole(String speakerName) {
        switch (SpeakerRole.resolve(speakerName, ownerName)) {
            case OWNER -> ownerCount++;
            case PARTNER -> partnerCount++;
        }
    }

    private void countType(String content) {
        MessageType type = MessageType.from(content);
        switch (type) {
            case TEXT -> textCount++;
            case EMOTICON -> emoticonCount++;
            case PHOTO -> photoCount++;
            default -> throw new IllegalStateException("분류되지 않은 메시지 종류: " + type);
        }
    }

    ParseStats toStats() {
        return new ParseStats(
                ownerCount,
                partnerCount,
                textCount,
                emoticonCount,
                photoCount,
                period.startedAt(),
                period.endedAt(),
                activeDays.size()
        );
    }
}
