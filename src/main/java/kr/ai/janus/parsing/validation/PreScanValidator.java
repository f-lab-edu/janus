package kr.ai.janus.parsing.validation;

import java.util.Comparator;
import java.util.List;

import kr.ai.janus.common.exception.BusinessException;
import kr.ai.janus.common.exception.ErrorCode;
import kr.ai.janus.parsing.model.PreScanSummary;
import kr.ai.janus.parsing.model.SpeakerCount;
import lombok.extern.slf4j.Slf4j;

/**
 * 1차 스캔 결과 검증
 */
@Slf4j
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
            log.info("1차 스캔 검증 실패: 화자 {}명 < 2명", topTwo.size());
            throw new BusinessException(ErrorCode.INSUFFICIENT_SPEAKERS);
        }

        validatePerSpeakerMessages(topTwo);
        validateTopTwoShare(summary, topTwo);
        return topTwo;
    }

    private void validateTotalMessages(PreScanSummary summary) {
        if (summary.messageCount() < thresholds.minTotalMessages()) {
            log.info("1차 스캔 검증 실패: 전체 {}건 < 기준 {}건",
                    summary.messageCount(), thresholds.minTotalMessages());
            throw new BusinessException(ErrorCode.INSUFFICIENT_TOTAL_MESSAGES);
        }
    }

    private void validatePerSpeakerMessages(List<SpeakerCount> topTwo) {
        long minCount = topTwo.stream()
                .mapToLong(SpeakerCount::messageCount)
                .min()
                .orElse(0);
        if (minCount < thresholds.minPerSpeakerMessages()) {
            log.info("1차 스캔 검증 실패: 화자별 최소 {}건 < 기준 {}건",
                    minCount, thresholds.minPerSpeakerMessages());
            throw new BusinessException(ErrorCode.INSUFFICIENT_MESSAGES_PER_SPEAKER);
        }
    }

    private void validateTopTwoShare(PreScanSummary summary, List<SpeakerCount> topTwo) {
        long topTwoMessageCount = topTwo.stream()
                .mapToLong(SpeakerCount::messageCount)
                .sum();
        double share = (double) topTwoMessageCount / summary.messageCount();
        if (share < thresholds.topTwoShareThreshold()) {
            log.info("1차 스캔 검증 실패: 상위 2인 비율 {} < 기준 {}",
                    share, thresholds.topTwoShareThreshold());
            throw new BusinessException(ErrorCode.INSUFFICIENT_TOP_TWO_SHARE);
        }
    }
}
