package kr.ai.janus.parsing.grammar;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import org.springframework.stereotype.Component;

import kr.ai.janus.common.exception.BusinessException;
import kr.ai.janus.common.exception.ErrorCode;
import kr.ai.janus.parsing.model.RawMessage;
import lombok.extern.slf4j.Slf4j;

/**
 * Mac 카카오톡 CSV(`Date,User,Message`)를 읽어 RawMessage 목록으로 만든다.
 */
@Component
@Slf4j
public final class CsvMessageParser {

    private static final char BOM = '\uFEFF';
    private static final String DATE_HEADER = "Date";
    private static final String USER_HEADER = "User";
    private static final String MESSAGE_HEADER = "Message";
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final CSVFormat FORMAT = CSVFormat.DEFAULT.builder()
            .setHeader()
            .setSkipHeaderRecord(true)
            .build();

    public List<RawMessage> parse(Reader reader) {
        List<RawMessage> messages = new ArrayList<>();

        try (CSVParser parser = CSVParser.parse(removeBom(reader), FORMAT)) {
            validateHeaders(parser);

            for (CSVRecord csvRow : parser) {
                messages.add(toMessage(csvRow));
            }
        } catch (IOException e) {
            log.error("CSV 읽기 실패", e);
            throw new BusinessException(ErrorCode.FILE_READ_FAILED, e);
        }

        return messages;
    }

    private void validateHeaders(CSVParser parser) {
        List<String> headers = parser.getHeaderNames();

        if (!hasRequiredHeaders(headers)) {
            log.warn("CSV 필수 컬럼 없음: {}", headers);
            throw new BusinessException(ErrorCode.INVALID_CSV_FORMAT);
        }
    }

    private boolean hasRequiredHeaders(List<String> headers) {
        return headers.contains(USER_HEADER)
                && headers.contains(MESSAGE_HEADER)
                && headers.contains(DATE_HEADER);
    }

    private RawMessage toMessage(CSVRecord csvRow) {
        try {
            String speakerName = csvRow.get(USER_HEADER);
            String text = csvRow.get(MESSAGE_HEADER);
            LocalDateTime sentAt = parseSentAt(csvRow.get(DATE_HEADER));
            return new RawMessage(speakerName, sentAt, text);
        } catch (RuntimeException e) {
            log.warn("CSV {}번째 행 파싱 실패", csvRow.getRecordNumber(), e);
            throw new BusinessException(ErrorCode.INVALID_CSV_FORMAT, e);
        }
    }

    private LocalDateTime parseSentAt(String raw) {
        return LocalDateTime.parse(raw.trim(), DATE_FORMAT);
    }

    /** 파일 맨 앞에 붙는 보이지 않는 문자(BOM)가 있으면 건너뛴다. 없으면 그대로 둔다. */
    private Reader removeBom(Reader in) throws IOException {
        PushbackReader reader = new PushbackReader(in, 1);
        int first = reader.read();
        if (isNormalChar(first)) {
            reader.unread(first);   // BOM이 아니면 읽은 문자를 되돌려 놓는다
        }
        return reader;
    }

    private boolean isNormalChar(int character) {
        return character != -1 && character != BOM;
    }
}
