package kr.ai.janus.parsing.model;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 1차 스캔에서 수집한 정보
 */
public record PreScanSummary(
        List<SpeakerCount> speakerCounts,
        LocalDateTime startedAt,
        LocalDateTime endedAt,
        long messageCount
) {
}
