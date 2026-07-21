package kr.ai.janus.parsing.classify;

import java.util.regex.Pattern;

import kr.ai.janus.parsing.model.MessageType;

/**
 * 메시지 내용을 종류로 분류한다.
 * 앵커(전체 일치)로만 판별한다 — 내용이 정확히 마커일 때만 첨부로 본다.
 * MVP(CSV): EMOTICON·PHOTO만 판별하고 나머지는 TEXT.
 */
public final class MessageTypeClassifier {

    private static final String EMOTICON_MARKER = "이모티콘";
    private static final Pattern PHOTO = Pattern.compile("사진( \\d+장)?");

    public MessageType classify(String content) {
        String text = content.strip();

        if (text.equals(EMOTICON_MARKER)) {
            return MessageType.EMOTICON;
        }
        if (PHOTO.matcher(text).matches()) {
            return MessageType.PHOTO;
        }
        return MessageType.TEXT;
    }
}
