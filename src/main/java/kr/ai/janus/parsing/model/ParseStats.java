package kr.ai.janus.parsing.model;

import java.time.LocalDateTime;

/**
 * 2차 스캔이 끝난 뒤 남는 기본 통계
 *
 * @param ownerCount     OWNER 메시지 수
 * @param partnerCount   PARTNER 메시지 수
 * @param textCount      TEXT 수
 * @param emoticonCount  EMOTICON 수
 * @param photoCount     PHOTO 수
 * @param startedAt      첫 메시지 시각
 * @param endedAt        마지막 메시지 시각
 * @param activeDayCount 메시지가 하나라도 있었던 날 수
 */
public record ParseStats(
        long ownerCount,
        long partnerCount,
        long textCount,
        long emoticonCount,
        long photoCount,
        LocalDateTime startedAt,
        LocalDateTime endedAt,
        long activeDayCount
) {

    public long analyzedMessages() {
        return ownerCount + partnerCount;
    }
}
