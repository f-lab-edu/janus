package kr.ai.janus.parsing.model;

import java.time.LocalDateTime;

/**
 * 파일에서 읽어 파싱한 원시 메시지 한 건
 */
public record RawMessage(
        String speakerName,
        LocalDateTime sentAt,
        String text
) {
}
