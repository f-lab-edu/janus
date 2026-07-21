package kr.ai.janus.parsing.model;

/**
 TEXT
 - 주요 feature 계산 대상
 - 분류되지 않은 내용은 모두 TEXT로 본다

 EMOTICON, PHOTO
 - 자체 빈도 지표에만 사용
 */
public enum MessageType {
    TEXT,
    EMOTICON,
    PHOTO
}
