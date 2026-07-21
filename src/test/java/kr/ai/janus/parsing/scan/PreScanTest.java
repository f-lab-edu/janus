package kr.ai.janus.parsing.scan;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import kr.ai.janus.parsing.grammar.CsvMessageParser;
import kr.ai.janus.parsing.model.PreScanSummary;
import kr.ai.janus.parsing.model.RawMessage;
import kr.ai.janus.parsing.model.SpeakerCount;

class PreScanTest {

    private final PreScan preScan = new PreScan();

    private RawMessage message(String speaker, LocalDateTime sentAt) {
        return new RawMessage(speaker, sentAt, "내용");
    }

    @Test
    void 화자별_메시지_수를_센다() {
        List<RawMessage> messages = List.of(
                message("민지", LocalDateTime.of(2026, 2, 10, 10, 0)),
                message("지훈", LocalDateTime.of(2026, 2, 10, 10, 1)),
                message("민지", LocalDateTime.of(2026, 2, 10, 10, 2))
        );

        PreScanSummary summary = preScan.summarize(messages);

        assertThat(summary.speakerCounts()).containsExactly(
                new SpeakerCount("민지", 2),
                new SpeakerCount("지훈", 1)
        );
        assertThat(summary.messageCount()).isEqualTo(3);
    }

    @Test
    void 시작과_종료_시각을_가장_이른_것과_가장_늦은_것으로_잡는다() {
        List<RawMessage> messages = List.of(
                message("민지", LocalDateTime.of(2026, 2, 10, 10, 5)),
                message("지훈", LocalDateTime.of(2026, 2, 10, 9, 0)),   // 가장 이름
                message("민지", LocalDateTime.of(2026, 2, 11, 23, 0))   // 가장 늦음
        );

        PreScanSummary summary = preScan.summarize(messages);

        assertThat(summary.startedAt()).isEqualTo(LocalDateTime.of(2026, 2, 10, 9, 0));
        assertThat(summary.endedAt()).isEqualTo(LocalDateTime.of(2026, 2, 11, 23, 0));
    }

    @Test
    void 빈_목록에서도_터지지_않고_사실대로_담는다() {
        PreScanSummary summary = preScan.summarize(List.of());

        assertThat(summary.messageCount()).isZero();
        assertThat(summary.speakerCounts()).isEmpty();
        assertThat(summary.startedAt()).isNull();
        assertThat(summary.endedAt()).isNull();
    }

    @Test
    void CSV_파일부터_1차_스캔까지_이어진다() {
        List<RawMessage> messages = parseFixture("fixtures/csv/simple.csv");

        PreScanSummary summary = preScan.summarize(messages);

        assertThat(summary.messageCount()).isEqualTo(10);
        assertThat(summary.speakerCounts())
                .extracting(SpeakerCount::name)
                .containsExactlyInAnyOrder("민지", "지훈");
    }

    private List<RawMessage> parseFixture(String path) {
        InputStream in = getClass().getClassLoader().getResourceAsStream(path);
        assertThat(in).as("픽스처 %s 존재", path).isNotNull();
        try (Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
            return new CsvMessageParser().parse(reader);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
