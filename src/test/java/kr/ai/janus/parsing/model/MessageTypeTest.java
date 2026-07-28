package kr.ai.janus.parsing.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class MessageTypeTest {

    @Test
    void 이모티콘_마커를_EMOTICON으로_분류한다() {
        assertThat(MessageType.from("이모티콘")).isEqualTo(MessageType.EMOTICON);
        assertThat(MessageType.from("이모티콘 ")).isEqualTo(MessageType.EMOTICON); // 후행 공백
    }

    @Test
    void 사진과_사진_N장을_PHOTO로_분류한다() {
        assertThat(MessageType.from("사진")).isEqualTo(MessageType.PHOTO);
        assertThat(MessageType.from("사진 3장")).isEqualTo(MessageType.PHOTO);
    }

    @Test
    void 마커가_문장에_섞여_있으면_TEXT다() {
        assertThat(MessageType.from("사진 보여줘")).isEqualTo(MessageType.TEXT);
        assertThat(MessageType.from("이모티콘 진짜 웃김")).isEqualTo(MessageType.TEXT);
    }

    @Test
    void 일반_대화는_TEXT다() {
        assertThat(MessageType.from("오늘도 고생했어")).isEqualTo(MessageType.TEXT);
    }
}
