package kr.ai.janus.parsing.validation;

/**
 * @param minTotalMessages      총 메시지 하한 — 미만이면 메시지 부족
 * @param minPerSpeakerMessages 두 사람 각각의 메시지 하한 — 미만이면 메시지 부족
 */
public record PreScanThresholds(
        int minTotalMessages,
        int minPerSpeakerMessages
) {

    public static PreScanThresholds defaults() {
        return new PreScanThresholds(50, 10);
    }
}
