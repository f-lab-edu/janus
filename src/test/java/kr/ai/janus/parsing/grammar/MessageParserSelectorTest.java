package kr.ai.janus.parsing.grammar;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;

import kr.ai.janus.common.exception.BusinessException;
import kr.ai.janus.common.exception.ErrorCode;

class MessageParserSelectorTest {

    private final CsvMessageParser csvParser = new CsvMessageParser();
    private final IosTxtMessageParser txtParser = new IosTxtMessageParser();
    private final MessageParserSelector selector =
            new MessageParserSelector(List.of(csvParser, txtParser));

    @Test
    void csv_파일이면_CSV_파서를_고른다() {
        assertThat(selector.select("KakaoTalk_Chat_2026-07-15.csv")).isSameAs(csvParser);
    }

    @Test
    void txt_파일이면_아이폰_txt_파서를_고른다() {
        assertThat(selector.select("Talk_2026.7.14 23:35-1.txt")).isSameAs(txtParser);
    }

    @Test
    void 확장자가_대문자여도_고른다() {
        assertThat(selector.select("CHAT.CSV")).isSameAs(csvParser);
    }

    @Test
    void 읽을_수_있는_파서가_없으면_UNSUPPORTED_FILE_FORMAT_예외를_던진다() {
        assertThatThrownBy(() -> selector.select("chat.pdf"))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.UNSUPPORTED_FILE_FORMAT);
    }

    @Test
    void 확장자가_없으면_UNSUPPORTED_FILE_FORMAT_예외를_던진다() {
        assertThatThrownBy(() -> selector.select("chat"))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.UNSUPPORTED_FILE_FORMAT);
    }

    @Test
    void 파일_이름이_없으면_UNSUPPORTED_FILE_FORMAT_예외를_던진다() {
        assertThatThrownBy(() -> selector.select(null))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.UNSUPPORTED_FILE_FORMAT);

        assertThatThrownBy(() -> selector.select("  "))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.UNSUPPORTED_FILE_FORMAT);
    }
}
