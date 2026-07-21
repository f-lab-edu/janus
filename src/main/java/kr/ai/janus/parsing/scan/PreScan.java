package kr.ai.janus.parsing.scan;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import kr.ai.janus.parsing.model.PreScanSummary;
import kr.ai.janus.parsing.model.RawMessage;
import kr.ai.janus.parsing.model.SpeakerCount;

public final class PreScan {

    public PreScanSummary summarize(List<RawMessage> messages) {
        Map<String, Long> messageCountBySpeaker = new LinkedHashMap<>();
        LocalDateTime startedAt = null;
        LocalDateTime endedAt = null;

        for (RawMessage message : messages) {
            messageCountBySpeaker.merge(message.speakerName(), 1L, Long::sum);

            LocalDateTime sentAt = message.sentAt();
            if (startedAt == null || sentAt.isBefore(startedAt)) {
                startedAt = sentAt;
            }
            if (endedAt == null || sentAt.isAfter(endedAt)) {
                endedAt = sentAt;
            }
        }

        List<SpeakerCount> speakerCounts = messageCountBySpeaker.entrySet().stream()
                .map(entry -> new SpeakerCount(entry.getKey(), entry.getValue()))
                .toList();

        return new PreScanSummary(speakerCounts, startedAt, endedAt, messages.size());
    }
}
