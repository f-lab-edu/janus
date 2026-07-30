package kr.ai.janus.parsing.validation;

import org.springframework.stereotype.Component;

import kr.ai.janus.common.exception.BusinessException;
import kr.ai.janus.common.exception.ErrorCode;
import kr.ai.janus.parsing.model.PreScanSummary;
import kr.ai.janus.parsing.model.SpeakerCount;
import lombok.extern.slf4j.Slf4j;

/**
 * 1차 스캔 결과 검증 + 본인 선택 검증
 */
@Component
@Slf4j
public final class PreScanValidator {

    /** 1:1 대화 = 두 명 */
    private static final int REQUIRED_SPEAKER_COUNT = 2;

    private final PreScanThresholds thresholds = PreScanThresholds.defaults();

    /** 분석 가능한 1:1 대화인지 검증한다 (본인 선택 전 단계에서 사용). */
    public void validateChat(PreScanSummary summary) {
        validateTotalMessages(summary);
        validateExactlyTwoSpeakers(summary);
        validatePerSpeakerMessages(summary);
    }

    /** 채팅 검증 + 본인(owner)이 두 화자 중 하나인지까지 검증한다. */
    public void validate(PreScanSummary summary, String ownerName) {
        validateChat(summary);
        validateOwnerIsParticipant(summary, ownerName);
    }

    private void validateTotalMessages(PreScanSummary summary) {
        if (isTotalBelowMinimum(summary.messageCount())) {
            log.info("1차 스캔 검증 실패: 전체 {}건 < 기준 {}건",
                    summary.messageCount(), thresholds.minTotalMessages());
            throw new BusinessException(ErrorCode.INSUFFICIENT_TOTAL_MESSAGES);
        }
    }

    private boolean isTotalBelowMinimum(long totalCount) {
        return totalCount < thresholds.minTotalMessages();
    }

    private void validateExactlyTwoSpeakers(PreScanSummary summary) {
        int speakerCount = summary.speakerCounts().size();
        if (speakerCount < REQUIRED_SPEAKER_COUNT) {
            log.info("1차 스캔 검증 실패: 화자 {}명 < {}명", speakerCount, REQUIRED_SPEAKER_COUNT);
            throw new BusinessException(ErrorCode.INSUFFICIENT_SPEAKERS);
        }
        if (speakerCount > REQUIRED_SPEAKER_COUNT) {
            log.info("1차 스캔 검증 실패: 화자 {}명 > {}명", speakerCount, REQUIRED_SPEAKER_COUNT);
            throw new BusinessException(ErrorCode.NOT_ONE_TO_ONE_CHAT);
        }
    }

    private void validatePerSpeakerMessages(PreScanSummary summary) {
        long minCount = summary.speakerCounts().stream()
                .mapToLong(SpeakerCount::messageCount)
                .min()
                .orElse(0);
        if (isAnySpeakerBelowMinimum(minCount)) {
            log.info("1차 스캔 검증 실패: 화자별 최소 {}건 < 기준 {}건",
                    minCount, thresholds.minPerSpeakerMessages());
            throw new BusinessException(ErrorCode.INSUFFICIENT_MESSAGES_PER_SPEAKER);
        }
    }

    private boolean isAnySpeakerBelowMinimum(long minCount) {
        return minCount < thresholds.minPerSpeakerMessages();
    }

    private void validateOwnerIsParticipant(PreScanSummary summary, String ownerName) {
        boolean participant = summary.speakerCounts().stream()
                .anyMatch(speaker -> speaker.name().equals(ownerName));
        if (!participant) {
            log.info("본인 검증 실패: 선택한 이름이 화자 목록에 없음");
            throw new BusinessException(ErrorCode.UNKNOWN_SPEAKER);
        }
    }
}
