package kr.ai.janus.parsing.grammar;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import kr.ai.janus.common.exception.BusinessException;
import kr.ai.janus.common.exception.ErrorCode;
import kr.ai.janus.parsing.model.RawMessage;

class CsvMessageParserTest {

    private final CsvMessageParser parser = new CsvMessageParser();

    private List<RawMessage> parseFixture(String path) {
        InputStream in = getClass().getClassLoader().getResourceAsStream(path);
        assertThat(in).as("픽스처 %s 존재", path).isNotNull();
        try (Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
            return parser.parse(reader);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void 헤더를_건너뛰고_모든_메시지를_읽는다() {
        List<RawMessage> messages = parseFixture("fixtures/csv/simple.csv");

        assertThat(messages).hasSize(10);
    }

    @Test
    void BOM을_제거하고_첫_메시지의_화자를_바르게_읽는다() {
        List<RawMessage> messages = parseFixture("fixtures/csv/simple.csv");

        RawMessage first = messages.getFirst();
        assertThat(first.speakerName()).isEqualTo("민지");
        assertThat(first.text()).isEqualTo("오늘도 고생했어");
    }

    @Test
    void 시각을_초까지_그대로_읽는다() {
        List<RawMessage> messages = parseFixture("fixtures/csv/simple.csv");

        assertThat(messages.getFirst().sentAt())
                .isEqualTo(LocalDateTime.of(2026, 2, 10, 22, 14, 5));
    }

    @Test
    void 따옴표_안의_쉼표와_이스케이프_따옴표를_보존한다() {
        List<RawMessage> messages = parseFixture("fixtures/csv/simple.csv");

        RawMessage lunch = messages.stream()
                .filter(m -> m.text().contains("점심"))
                .findFirst()
                .orElseThrow();
        assertThat(lunch.text()).isEqualTo("점심 뭐 먹지, 근처에 \"새로 생긴 집\" 갈까?");
    }

    @Test
    void 헤더에_필요한_컬럼이_없으면_INVALID_CSV_FORMAT_예외를_던진다() {
        // 카톡 CSV가 아닌 엉뚱한 파일 — Date/User/Message 컬럼 없음
        String wrongFile = """
                이름,나이
                민지,25
                """;

        assertThatThrownBy(() -> parser.parse(new StringReader(wrongFile)))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_CSV_FORMAT);
    }

    @Test
    void 날짜_형식이_깨진_행이_있으면_INVALID_CSV_FORMAT_예외를_던진다() {
        String brokenDate = """
                Date,User,Message
                2026-02-10 22:14:05,민지,정상 메시지
                이건 날짜가 아님,지훈,깨진 행
                """;

        assertThatThrownBy(() -> parser.parse(new StringReader(brokenDate)))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_CSV_FORMAT);
    }
}
