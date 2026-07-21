package kr.ai.janus.parsing.validation;

import java.util.Comparator;
import java.util.List;

import kr.ai.janus.common.exception.BusinessException;
import kr.ai.janus.common.exception.ErrorCode;
import kr.ai.janus.parsing.model.PreScanSummary;
import kr.ai.janus.parsing.model.SpeakerCount;

/**
 * 1차 스캔 결과 검증
 */
public final class PreScanValidator {

    private final PreScanThresholds thresholds;

    public PreScanValidator(PreScanThresholds thresholds) {
        this.thresholds = thresholds;
    }

    /** @return 상위 2인 (메시지 많은 순) */
    public List<SpeakerCount> validateAndSelectParticipants(PreScanSummary summary) {
        validateTotalMessages(summary);

        List<SpeakerCount> topTwo = summary.speakerCounts().stream()
                .sorted(Comparator.comparingLong(SpeakerCount::messageCount).reversed())
                .limit(2)
                .toList();

        if (topTwo.size() < 2) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_SPEAKERS);
        }

        validatePerSpeakerMessages(topTwo);
        validateTopTwoShare(summary, topTwo);
        return topTwo;
    }

    private void validateTotalMessages(PreScanSummary summary) {
        if (summary.messageCount() < thresholds.minTotalMessages()) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_TOTAL_MESSAGES);
        }
    }

    private void validatePerSpeakerMessages(List<SpeakerCount> topTwo) {
        boolean insufficient = topTwo.stream()
                .anyMatch(speaker -> speaker.messageCount() < thresholds.minPerSpeakerMessages());
        if (insufficient) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_MESSAGES_PER_SPEAKER);
        }
    }

    private void validateTopTwoShare(PreScanSummary summary, List<SpeakerCount> topTwo) {
        long topTwoMessageCount = topTwo.stream()
                .mapToLong(SpeakerCount::messageCount)
                .sum();
        double share = (double) topTwoMessageCount / summary.messageCount();
        if (share < thresholds.topTwoShareThreshold()) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_TOP_TWO_SHARE);
        }
    }
}
