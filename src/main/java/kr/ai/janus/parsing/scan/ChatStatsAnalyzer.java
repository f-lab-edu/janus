package kr.ai.janus.parsing.scan;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import kr.ai.janus.parsing.classify.MessageTypeClassifier;
import kr.ai.janus.parsing.model.MessageType;
import kr.ai.janus.parsing.model.ParseStats;
import kr.ai.janus.parsing.model.RawMessage;
import kr.ai.janus.parsing.model.SpeakerMapping;
import kr.ai.janus.parsing.model.SpeakerRole;

public final class ChatStatsAnalyzer {

    private final MessageTypeClassifier classifier;

    public ChatStatsAnalyzer(MessageTypeClassifier classifier) {
        this.classifier = classifier;
    }

    public ParseStats analyze(List<RawMessage> messages, SpeakerMapping speakers) {
        long ownerCount = 0;
        long partnerCount = 0;
        long textCount = 0;
        long emoticonCount = 0;
        long photoCount = 0;
        MessagePeriodAccumulator period = new MessagePeriodAccumulator();
        Set<LocalDate> activeDays = new HashSet<>();

        for (RawMessage message : messages) {
            SpeakerRole role = speakers.roleOf(message.speakerName());
            if (role == SpeakerRole.EXCLUDED) {
                continue;   // 소수 화자는 모든 통계에서 제외
            }

            switch (role) {
                case OWNER -> ownerCount++;
                case PARTNER -> partnerCount++;
                default -> throw new IllegalStateException("처리되지 않은 화자 역할: " + role);
            }

            MessageType type = classifier.classify(message.text());
            switch (type) {
                case TEXT -> textCount++;
                case EMOTICON -> emoticonCount++;
                case PHOTO -> photoCount++;
                default -> throw new IllegalStateException("분류되지 않은 메시지 종류: " + type);
            }

            period.accumulate(message.sentAt());
            activeDays.add(message.sentAt().toLocalDate());
        }

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
