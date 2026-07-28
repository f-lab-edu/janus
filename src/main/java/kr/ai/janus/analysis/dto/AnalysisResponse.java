package kr.ai.janus.analysis.dto;

import java.time.LocalDateTime;

import kr.ai.janus.parsing.model.ParseStats;

public record AnalysisResponse(
        long messageCount,
        long ownerCount,
        long partnerCount,
        long textCount,
        long emoticonCount,
        long photoCount,
        LocalDateTime startedAt,
        LocalDateTime endedAt,
        long activeDayCount) {

    public static AnalysisResponse from(ParseStats stats) {
        return new AnalysisResponse(
                stats.analyzedMessages(),
                stats.ownerCount(),
                stats.partnerCount(),
                stats.textCount(),
                stats.emoticonCount(),
                stats.photoCount(),
                stats.startedAt(),
                stats.endedAt(),
                stats.activeDayCount());
    }
}
