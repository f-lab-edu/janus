package kr.ai.janus.parsing.validation;

/**
 * @param minTotalMessages      총 메시지 하한 — 미만이면 메시지 부족
 * @param minPerSpeakerMessages 상위 2인 각각의 메시지 하한 — 미만이면 메시지 부족
 * @param topTwoShareThreshold  상위 2인 메시지 수 ÷ 전체 — 미만이면 단톡으로 간주
 */
public record PreScanThresholds(
        int minTotalMessages,
        int minPerSpeakerMessages,
        double topTwoShareThreshold
) {

    public static PreScanThresholds defaults() {
        return new PreScanThresholds(50, 10, 0.95);
    }
}
