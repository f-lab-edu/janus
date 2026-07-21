package kr.ai.janus.parsing.grammar;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import kr.ai.janus.parsing.model.RawMessage;

/**
 * Mac 카카오톡 CSV(`Date,User,Message`)를 읽어 RawMessage 목록으로 만든다.
 */
public final class CsvGrammar {

    private static final char BOM = '﻿';
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public List<RawMessage> parse(Reader reader) {
        List<RawMessage> messages = new ArrayList<>();
        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .build();

        try (CSVParser parser = CSVParser.parse(stripBom(reader), format)) {
            for (CSVRecord record : parser) {
                String user = record.get("User");
                String message = record.get("Message");
                LocalDateTime sentAt = parseSentAt(record.get("Date"));
                messages.add(new RawMessage(user, sentAt, message));
            }
        } catch (IOException e) {
            throw new UncheckedIOException("CSV를 읽는 중 오류가 발생했습니다.", e);
        }
        return messages;
    }

    private LocalDateTime parseSentAt(String raw) {
        return LocalDateTime.parse(raw.trim(), DATE_FORMAT).withSecond(0).withNano(0);
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
