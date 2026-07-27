package kr.ai.janus.parsing.model;

import java.util.regex.Pattern;

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
    PHOTO;

    private static final String EMOTICON_MARKER = "이모티콘";
    private static final Pattern PHOTO_MARKER = Pattern.compile("사진( \\d+장)?");

    /**
     * MVP: EMOTICON, PHOTO만 판별하고 나머지는 TEXT
     */
    public static MessageType from(String content) {
        String text = content.strip();

        if (isEmoticon(text)) {
            return EMOTICON;
        }
        if (isPhoto(text)) {
            return PHOTO;
        }
        return TEXT;
    }

    private static boolean isEmoticon(String text) {
        return text.equals(EMOTICON_MARKER);
    }

    private static boolean isPhoto(String text) {
        return PHOTO_MARKER.matcher(text).matches();
    }
}
