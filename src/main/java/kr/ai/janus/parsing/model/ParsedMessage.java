package kr.ai.janus.parsing.model;

import java.time.LocalDateTime;

/**
 * 2차 스캔이 만들어 내는 메시지 한 건
 */
public record ParsedMessage(
        SpeakerRole role,
        LocalDateTime sentAt,
        MessageType type,
        String content
) {
}
