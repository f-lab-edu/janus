package kr.ai.janus.parsing.model;

/**
 * 1차 스캔에서 집계한 화자 한 명의 메시지 수
 */
public record SpeakerCount(
        String name,
        long messageCount
) {
}
