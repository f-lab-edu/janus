package kr.ai.janus.parsing.grammar;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import kr.ai.janus.parsing.model.RawMessage;

class CsvGrammarTest {

    private final CsvGrammar grammar = new CsvGrammar();

    private List<RawMessage> parseFixture(String path) {
        InputStream in = getClass().getClassLoader().getResourceAsStream(path);
        assertThat(in).as("픽스처 %s 존재", path).isNotNull();
        try (Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
            return grammar.parse(reader);
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
    void 초_단위_시각을_분으로_절삭한다() {
        List<RawMessage> messages = parseFixture("fixtures/csv/simple.csv");

        // 원본: 2026-02-10 22:14:05 → 초 버림 → 22:14:00
        assertThat(messages.getFirst().sentAt())
                .isEqualTo(LocalDateTime.of(2026, 2, 10, 22, 14, 0));
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
}
