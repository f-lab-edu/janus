package kr.ai.janus.parsing.grammar;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import kr.ai.janus.common.exception.BusinessException;
import kr.ai.janus.common.exception.ErrorCode;
import kr.ai.janus.parsing.model.RawMessage;
import lombok.extern.slf4j.Slf4j;

/**
 * Mac 카카오톡 CSV(`Date,User,Message`)를 읽어 RawMessage 목록으로 만든다.
 */
@Slf4j
public final class CsvMessageParser {

    private static final char BOM = '\uFEFF';
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final CSVFormat FORMAT = CSVFormat.DEFAULT.builder()
            .setHeader()
            .setSkipHeaderRecord(true)
            .build();

    public List<RawMessage> parse(Reader reader) {
        List<RawMessage> messages = new ArrayList<>();

        try (CSVParser parser = CSVParser.parse(stripBom(reader), FORMAT)) {
            for (CSVRecord csvRow : parser) {
                messages.add(toMessage(csvRow));
            }
        } catch (IOException e) {
            log.error("CSV 읽기 실패", e);
            throw new BusinessException(ErrorCode.FILE_READ_FAILED, e);
        }

        return messages;
    }

    private RawMessage toMessage(CSVRecord csvRow) {
        try {
            String speakerName = csvRow.get("User");
            String text = csvRow.get("Message");
            LocalDateTime sentAt = parseSentAt(csvRow.get("Date"));
            return new RawMessage(speakerName, sentAt, text);
        } catch (IllegalArgumentException | DateTimeParseException e) {
            log.warn("CSV {}번째 행 파싱 실패", csvRow.getRecordNumber(), e);
            throw new BusinessException(ErrorCode.INVALID_CSV_FORMAT, e);
        }
    }

    private LocalDateTime parseSentAt(String raw) {
        return LocalDateTime.parse(raw.trim(), DATE_FORMAT);
    }

    /** 파일 맨 앞의 BOM이 있으면 건너뛴다. 없으면 그대로 둔다. */
    private Reader stripBom(Reader in) throws IOException {
        PushbackReader pushback = new PushbackReader(in, 1);
        int first = pushback.read();
        if (first != -1 && first != BOM) {
            pushback.unread(first);
        }
        return pushback;
    }
}
