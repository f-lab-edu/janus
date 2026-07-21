package kr.ai.janus.parsing.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import kr.ai.janus.common.exception.BusinessException;
import kr.ai.janus.common.exception.ErrorCode;
import kr.ai.janus.parsing.model.PreScanSummary;
import kr.ai.janus.parsing.model.SpeakerCount;

class PreScanValidatorTest {

    private final PreScanValidator validator =
            new PreScanValidator(PreScanThresholds.defaults());  // 50, 10, 0.95

    /** 검증에 필요한 최소 조건을 만족하는 정상 요약 */
    private PreScanSummary summaryOf(List<SpeakerCount> speakers) {
        long total = speakers.stream().mapToLong(SpeakerCount::messageCount).sum();
        return new PreScanSummary(
                speakers,
                LocalDateTime.of(2026, 2, 10, 9, 0),
                LocalDateTime.of(2026, 2, 11, 23, 0),
                total
        );
    }

    @Test
    void 조건을_만족하면_상위_2인을_많은_순으로_돌려준다() {
        PreScanSummary summary = summaryOf(List.of(
                new SpeakerCount("민지", 40),
                new SpeakerCount("지훈", 60)
        ));

        List<SpeakerCount> topTwo = validator.validateAndSelectParticipants(summary);

        assertThat(topTwo).extracting(SpeakerCount::name).containsExactly("지훈", "민지");
    }

    @Test
    void 소수_화자가_섞여도_상위_2인만_뽑는다() {
        PreScanSummary summary = summaryOf(List.of(
                new SpeakerCount("민지", 500),
                new SpeakerCount("지훈", 500),
                new SpeakerCount("민지🩷", 20)   // 이름 잔재 — 상위 2인에서 제외
        ));

        List<SpeakerCount> topTwo = validator.validateAndSelectParticipants(summary);

        assertThat(topTwo).extracting(SpeakerCount::name).containsExactly("민지", "지훈");
    }

    @Test
    void 전체_메시지가_하한보다_적으면_INSUFFICIENT_TOTAL_MESSAGES() {
        PreScanSummary summary = summaryOf(List.of(
                new SpeakerCount("민지", 20),
                new SpeakerCount("지훈", 20)   // 합 40 < 50
        ));

        assertThatThrownBy(() -> validator.validateAndSelectParticipants(summary))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INSUFFICIENT_TOTAL_MESSAGES);
    }

    @Test
    void 화자가_한_명뿐이면_INSUFFICIENT_SPEAKERS() {
        PreScanSummary summary = summaryOf(List.of(
                new SpeakerCount("민지", 100)   // 혼자
        ));

        assertThatThrownBy(() -> validator.validateAndSelectParticipants(summary))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INSUFFICIENT_SPEAKERS);
    }

    @Test
    void 상위_2인_중_한_명이_최소_미달이면_INSUFFICIENT_MESSAGES_PER_SPEAKER() {
        PreScanSummary summary = summaryOf(List.of(
                new SpeakerCount("민지", 95),
                new SpeakerCount("지훈", 5)   // 5 < 10
        ));

        assertThatThrownBy(() -> validator.validateAndSelectParticipants(summary))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INSUFFICIENT_MESSAGES_PER_SPEAKER);
    }

    @Test
    void 상위_2인_점유율이_낮으면_INSUFFICIENT_TOP_TWO_SHARE() {
        // 상위 2인 60 / 전체 200 = 30% < 95% → 단톡으로 간주
        PreScanSummary summary = summaryOf(List.of(
                new SpeakerCount("민지", 30),
                new SpeakerCount("지훈", 30),
                new SpeakerCount("철수", 30),
                new SpeakerCount("영희", 30),
                new SpeakerCount("동수", 30),
                new SpeakerCount("수민", 30),
                new SpeakerCount("기타", 20)
        ));

        assertThatThrownBy(() -> validator.validateAndSelectParticipants(summary))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INSUFFICIENT_TOP_TWO_SHARE);
    }
}
