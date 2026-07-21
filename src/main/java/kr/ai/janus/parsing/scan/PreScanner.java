package kr.ai.janus.parsing.scan;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import kr.ai.janus.parsing.model.PreScanSummary;
import kr.ai.janus.parsing.model.RawMessage;
import kr.ai.janus.parsing.model.SpeakerCount;

public final class PreScanner {

    public PreScanSummary summarize(List<RawMessage> messages) {
        Map<String, Long> messageCountBySpeaker = new LinkedHashMap<>();
        MessagePeriodAccumulator period = new MessagePeriodAccumulator();

        for (RawMessage message : messages) {
            messageCountBySpeaker.merge(message.speakerName(), 1L, Long::sum);
            period.accumulate(message.sentAt());
        }

        List<SpeakerCount> speakerCounts = messageCountBySpeaker.entrySet().stream()
                .map(entry -> new SpeakerCount(entry.getKey(), entry.getValue()))
                .toList();

        return new PreScanSummary(speakerCounts, period.startedAt(), period.endedAt(), messages.size());
    }
}
