package kr.ai.janus.parsing.validation;

import static org.assertj.core.api.Assertions.assertThatCode;
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
            new PreScanValidator(PreScanThresholds.defaults());  // 50, 10

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
    void 정확히_두_명이고_owner가_그중_하나면_통과한다() {
        PreScanSummary summary = summaryOf(List.of(
                new SpeakerCount("민지", 40),
                new SpeakerCount("지훈", 60)
        ));

        assertThatCode(() -> validator.validate(summary, "민지"))
                .doesNotThrowAnyException();
    }

    @Test
    void validateChat은_owner_없이_채팅만_검증하고_정상이면_통과한다() {
        PreScanSummary summary = summaryOf(List.of(
                new SpeakerCount("민지", 40),
                new SpeakerCount("지훈", 60)
        ));

        assertThatCode(() -> validator.validateChat(summary))
                .doesNotThrowAnyException();
    }

    @Test
    void validateChat도_화자가_세_명_이상이면_NOT_ONE_TO_ONE_CHAT() {
        PreScanSummary summary = summaryOf(List.of(
                new SpeakerCount("민지", 40),
                new SpeakerCount("지훈", 40),
                new SpeakerCount("영수", 40)
        ));

        assertThatThrownBy(() -> validator.validateChat(summary))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.NOT_ONE_TO_ONE_CHAT);
    }

    @Test
    void 전체_메시지가_하한보다_적으면_INSUFFICIENT_TOTAL_MESSAGES() {
        PreScanSummary summary = summaryOf(List.of(
                new SpeakerCount("민지", 20),
                new SpeakerCount("지훈", 20)   // 합 40 < 50
        ));

        assertThatThrownBy(() -> validator.validate(summary, "민지"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INSUFFICIENT_TOTAL_MESSAGES);
    }

    @Test
    void 화자가_한_명뿐이면_INSUFFICIENT_SPEAKERS() {
        PreScanSummary summary = summaryOf(List.of(
                new SpeakerCount("민지", 100)   // 혼자
        ));

        assertThatThrownBy(() -> validator.validate(summary, "민지"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INSUFFICIENT_SPEAKERS);
    }

    @Test
    void 화자가_세_명_이상이면_NOT_ONE_TO_ONE_CHAT() {
        PreScanSummary summary = summaryOf(List.of(
                new SpeakerCount("민지", 40),
                new SpeakerCount("지훈", 40),
                new SpeakerCount("영수", 40)   // 단톡
        ));

        assertThatThrownBy(() -> validator.validate(summary, "민지"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.NOT_ONE_TO_ONE_CHAT);
    }

    @Test
    void 두_명_중_한_명이_최소_미달이면_INSUFFICIENT_MESSAGES_PER_SPEAKER() {
        PreScanSummary summary = summaryOf(List.of(
                new SpeakerCount("민지", 95),
                new SpeakerCount("지훈", 5)   // 5 < 10
        ));

        assertThatThrownBy(() -> validator.validate(summary, "민지"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INSUFFICIENT_MESSAGES_PER_SPEAKER);
    }

    @Test
    void 선택한_본인이_화자가_아니면_UNKNOWN_SPEAKER() {
        PreScanSummary summary = summaryOf(List.of(
                new SpeakerCount("민지", 40),
                new SpeakerCount("지훈", 60)
        ));

        assertThatThrownBy(() -> validator.validate(summary, "철수"))   // 참여자 아님
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.UNKNOWN_SPEAKER);
    }
}
