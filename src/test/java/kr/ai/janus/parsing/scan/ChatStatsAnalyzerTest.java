package kr.ai.janus.parsing.scan;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import kr.ai.janus.parsing.classify.MessageTypeClassifier;
import kr.ai.janus.parsing.grammar.CsvMessageParser;
import kr.ai.janus.parsing.model.ParseStats;
import kr.ai.janus.parsing.model.RawMessage;
import kr.ai.janus.parsing.model.SpeakerMapping;

class ChatStatsAnalyzerTest {

    private final ChatStatsAnalyzer analyzer = new ChatStatsAnalyzer(new MessageTypeClassifier());

    private RawMessage message(String speaker, LocalDateTime sentAt, String text) {
        return new RawMessage(speaker, sentAt, text);
    }

    @Test
    void OWNER와_PARTNER_메시지를_역할별로_센다() {
        List<RawMessage> messages = List.of(
                message("민지", LocalDateTime.of(2026, 2, 10, 10, 0), "안녕"),
                message("지훈", LocalDateTime.of(2026, 2, 10, 10, 1), "안녕!"),
                message("민지", LocalDateTime.of(2026, 2, 10, 10, 2), "뭐해")
        );

        ParseStats stats = analyzer.analyze(messages, new SpeakerMapping("민지", "지훈"));

        assertThat(stats.ownerCount()).isEqualTo(2);
        assertThat(stats.partnerCount()).isEqualTo(1);
        assertThat(stats.analyzedMessages()).isEqualTo(3);
    }

    @Test
    void EXCLUDED_화자의_메시지는_모든_통계에서_빠진다() {
        List<RawMessage> messages = List.of(
                message("민지", LocalDateTime.of(2026, 2, 10, 10, 0), "안녕"),
                message("지훈", LocalDateTime.of(2026, 2, 10, 10, 1), "안녕!"),
                // 소수 화자 — 기간·활성일에도 영향을 주면 안 된다
                message("영수", LocalDateTime.of(2026, 3, 1, 23, 0), "저도 끼어도 되나요")
        );

        ParseStats stats = analyzer.analyze(messages, new SpeakerMapping("민지", "지훈"));

        assertThat(stats.analyzedMessages()).isEqualTo(2);
        assertThat(stats.endedAt()).isEqualTo(LocalDateTime.of(2026, 2, 10, 10, 1));
        assertThat(stats.activeDayCount()).isEqualTo(1);
    }

    @Test
    void 메시지_종류별로_센다() {
        List<RawMessage> messages = List.of(
                message("민지", LocalDateTime.of(2026, 2, 10, 10, 0), "일반 텍스트"),
                message("지훈", LocalDateTime.of(2026, 2, 10, 10, 1), "이모티콘"),
                message("민지", LocalDateTime.of(2026, 2, 10, 10, 2), "사진 3장")
        );

        ParseStats stats = analyzer.analyze(messages, new SpeakerMapping("민지", "지훈"));

        assertThat(stats.textCount()).isEqualTo(1);
        assertThat(stats.emoticonCount()).isEqualTo(1);
        assertThat(stats.photoCount()).isEqualTo(1);
    }

    @Test
    void CSV_파일부터_2차_스캔까지_이어져_전체_통계가_나온다() {
        List<RawMessage> messages = parseFixture("fixtures/csv/simple.csv");

        ParseStats stats = analyzer.analyze(messages, new SpeakerMapping("민지", "지훈"));

        assertThat(stats.ownerCount()).isEqualTo(5);
        assertThat(stats.partnerCount()).isEqualTo(5);
        assertThat(stats.analyzedMessages()).isEqualTo(10);
        // "보이스톡 04:12"는 MVP에서 TEXT로 본다
        assertThat(stats.textCount()).isEqualTo(8);
        assertThat(stats.emoticonCount()).isEqualTo(1);
        assertThat(stats.photoCount()).isEqualTo(1);
        assertThat(stats.startedAt()).isEqualTo(LocalDateTime.of(2026, 2, 10, 22, 14, 5));
        assertThat(stats.endedAt()).isEqualTo(LocalDateTime.of(2026, 2, 11, 22, 41, 0));
        assertThat(stats.activeDayCount()).isEqualTo(2);
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
