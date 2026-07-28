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

    public static ParticipantsResponse from(PreScanSummary summary) {
        List<String> speakers = summary.speakerCounts().stream()
                .map(SpeakerCount::name)
                .toList();
        return new ParticipantsResponse(
                speakers, summary.messageCount(), summary.startedAt(), summary.endedAt());
    }
}
