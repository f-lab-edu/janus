package kr.ai.janus.parsing.grammar;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.StringReader;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import kr.ai.janus.common.exception.BusinessException;
import kr.ai.janus.common.exception.ErrorCode;
import kr.ai.janus.parsing.model.RawMessage;

class IosTxtMessageParserTest {

    private static final String BOM = "\uFEFF";

    private static final String SAMPLE = """
            Talk_2026.7.14 23:35-1.txt
            저장한 날짜 : 2026. 7. 15. 오전 3:01



            2025년 5월 22일 목요일
            2025. 5. 22. 오전 12:05, 민지 : 자니?
            2025. 5. 22. 오후 12:10, 지훈 : 점심 먹는 중
            2025. 5. 22. 오후 4:02, 민지 : 오늘
            밥 어디서
            먹을까
            2025. 5. 22. 오후 4:03, 지훈🐰 : 어디서 먹을까?
            2025. 5. 22. 오후 4:04, 민지 : 링크 보낼게 : https://example.com
            2025. 5. 22. 오후 4:05, 지훈🐰 : 이모티콘
            2025. 5. 22. 오후 4:06, 민지 : 사진 5장
            2025년 5월 23일 금요일
            2025. 5. 23. 오전 9:00, 지훈🐰 : 굿모닝
            """;

    private final IosTxtMessageParser parser = new IosTxtMessageParser();

    private List<RawMessage> parse(String text) {
        return parser.parse(new StringReader(text));
    }

    @Test
    void 화자와_시각과_내용을_읽는다() {
        RawMessage first = parse(SAMPLE).getFirst();

        assertThat(first.speakerName()).isEqualTo("민지");
        assertThat(first.sentAt()).isEqualTo(LocalDateTime.of(2025, 5, 22, 0, 5));
        assertThat(first.text()).isEqualTo("자니?");
    }

    @Test
    void 맨_앞_안내_줄과_날짜_구분_줄을_버린다() {
        // 안내 5줄, 날짜 구분 2줄, 이어지는 줄 2줄을 빼면 메시지는 8건이다
        assertThat(parse(SAMPLE)).hasSize(8);
    }

    @Test
    void 오전_12시는_자정으로_오후_12시는_정오로_바꾼다() {
        List<RawMessage> messages = parse(SAMPLE);

        assertThat(messages.get(0).sentAt()).isEqualTo(LocalDateTime.of(2025, 5, 22, 0, 5));
        assertThat(messages.get(1).sentAt()).isEqualTo(LocalDateTime.of(2025, 5, 22, 12, 10));
        assertThat(messages.get(2).sentAt()).isEqualTo(LocalDateTime.of(2025, 5, 22, 16, 2));
    }

    @Test
    void 기기가_24시간제면_오전_오후_없이_내보낸다() {
        String h24 = """
                2026. 8. 13. 14:19, 민지 : 오후
                2026. 8. 13. 0:07, 민지 : 자정
                2026. 8. 13. 9:30, 민지 : 오전
                """;

        List<RawMessage> messages = parse(h24);

        assertThat(messages).hasSize(3);
        assertThat(messages.get(0).sentAt()).isEqualTo(LocalDateTime.of(2026, 8, 13, 14, 19));
        assertThat(messages.get(1).sentAt()).isEqualTo(LocalDateTime.of(2026, 8, 13, 0, 7));
        assertThat(messages.get(2).sentAt()).isEqualTo(LocalDateTime.of(2026, 8, 13, 9, 30));
    }

    @Test
    void 여러_줄_메시지를_한_건으로_합치고_줄바꿈을_남긴다() {
        RawMessage multiLine = parse(SAMPLE).get(2);

        assertThat(multiLine.text()).isEqualTo("오늘\n밥 어디서\n먹을까");
    }

    @Test
    void 날짜_구분_줄은_앞_메시지에_붙지_않는다() {
        // "사진 5장" 바로 다음 줄이 날짜 구분 줄이다
        RawMessage beforeDivider = parse(SAMPLE).get(6);

        assertThat(beforeDivider.text()).isEqualTo("사진 5장");
    }

    @Test
    void 내용에_구분자가_또_있으면_맨_앞에서_나눈다() {
        RawMessage withSeparator = parse(SAMPLE).get(4);

        assertThat(withSeparator.speakerName()).isEqualTo("민지");
        assertThat(withSeparator.text()).isEqualTo("링크 보낼게 : https://example.com");
    }

    @Test
    void 이름에_이모지가_있어도_읽는다() {
        RawMessage withEmoji = parse(SAMPLE).get(3);

        assertThat(withEmoji.speakerName()).isEqualTo("지훈🐰");
    }

    @Test
    void 이모티콘과_사진_표시를_내용_그대로_담는다() {
        List<RawMessage> messages = parse(SAMPLE);

        assertThat(messages.get(5).text()).isEqualTo("이모티콘");
        assertThat(messages.get(6).text()).isEqualTo("사진 5장");
    }

    @Test
    void BOM이_있든_없든_결과가_같다() {
        assertThat(parse(BOM + SAMPLE)).isEqualTo(parse(SAMPLE));
    }

    @Test
    void 줄바꿈이_CRLF여도_결과가_같다() {
        assertThat(parse(SAMPLE.replace("\n", "\r\n"))).isEqualTo(parse(SAMPLE));
    }

    @Test
    void 카카오톡_파일이_아니면_INVALID_TXT_FORMAT_예외를_던진다() {
        String notKakaoTalk = """
                이건 그냥 메모장 파일
                줄이 여러 개 있지만 카카오톡 형식이 아니다
                """;

        assertThatThrownBy(() -> parse(notKakaoTalk))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_TXT_FORMAT);
    }

    @Test
    void 빈_파일이면_INVALID_TXT_FORMAT_예외를_던진다() {
        assertThatThrownBy(() -> parse(""))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_TXT_FORMAT);
    }

    @Test
    void txt_파일만_읽을_수_있다고_답한다() {
        assertThat(parser.supports("Talk_2026.7.14 23:35-1.txt")).isTrue();
        assertThat(parser.supports("TALK.TXT")).isTrue();
        assertThat(parser.supports("chat.csv")).isFalse();
        assertThat(parser.supports("chat.zip")).isFalse();
    }
}
