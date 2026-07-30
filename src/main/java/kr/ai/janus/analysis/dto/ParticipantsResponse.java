package kr.ai.janus.analysis.dto;

import java.time.LocalDateTime;
import java.util.List;

import kr.ai.janus.parsing.model.PreScanSummary;
import kr.ai.janus.parsing.model.SpeakerCount;

public record ParticipantsResponse(
        List<String> speakers,
        long messageCount,
        LocalDateTime startedAt,
        LocalDateTime endedAt) {

    private ParticipantsResponse(PreScanSummary summary) {
        this(
                summary.speakerCounts().stream()
                        .map(SpeakerCount::name)
                        .toList(),
                summary.messageCount(),
                summary.startedAt(),
                summary.endedAt()
        );
    }

    public static ParticipantsResponse from(PreScanSummary summary) {
        return new ParticipantsResponse(summary);
    }
}
