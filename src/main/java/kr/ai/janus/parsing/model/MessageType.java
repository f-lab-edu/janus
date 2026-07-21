package kr.ai.janus.parsing.model;

/**
 TEXT
 - 주요 feature 계산 대상

 EMOTICON, PHOTO
 - 자체 빈도 지표에만 사용

 ETC
 - 첨부, 통화, 카카오페이, 삭제 메시지 등 자동 문구
 - 모든 분석 통계와 evidence에서 제외
 */
public enum MessageType {
    TEXT,
    EMOTICON,
    PHOTO,
    ETC
}
