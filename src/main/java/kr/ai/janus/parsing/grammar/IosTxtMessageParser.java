package kr.ai.janus.parsing.grammar;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import kr.ai.janus.common.exception.BusinessException;
import kr.ai.janus.common.exception.ErrorCode;
import kr.ai.janus.parsing.model.RawMessage;
import lombok.extern.slf4j.Slf4j;

/**
 * 아이폰 카카오톡 txt(`yyyy. M. d. [오전/오후] h:mm, 이름 : 내용`)를 읽어 RawMessage 목록으로 만든다.
 */
@Component
@Slf4j
public final class IosTxtMessageParser implements MessageParser {

    private static final String TXT_EXTENSION = ".txt";

    private static final String BOM = "\uFEFF";

    /**
     * 예시) 2026. 5. 22. 오후 4:02, 민지 : 점심 뭐 먹었어?   (기기가 12시간제)
     *      2026. 5. 22. 16:02, 민지 : 점심 뭐 먹었어?      (기기가 24시간제)
     */
    private static final Pattern MESSAGE_START_LINE = Pattern.compile(
            "^(?<sentAt>\\d{4}\\. \\d{1,2}\\. \\d{1,2}\\. (?:(?:오전|오후) )?\\d{1,2}:\\d{2}), (?<speakerName>.+?) : (?<text>.*)$");

    /** 예시) 2026년 5월 22일 목요일 */
    private static final Pattern DATE_DIVIDER_LINE = Pattern.compile(
            "^\\d{4}년 \\d{1,2}월 \\d{1,2}일 .+$");

    /**
     * 예시) 2026. 5. 22. 오후 4:02   (기기가 12시간제)
     *      2026. 5. 22. 16:02      (기기가 24시간제)
     */
    private static final DateTimeFormatter SENT_AT_FORMAT =
            DateTimeFormatter.ofPattern("yyyy. M. d. [a h:mm][H:mm]", Locale.KOREAN);

    @Override
    public boolean supports(String fileName) {
        return fileName.toLowerCase(Locale.ROOT).endsWith(TXT_EXTENSION);
    }

    @Override
    public List<RawMessage> parse(Reader reader) {
        List<RawMessage> messages = readMessages(reader);

        if (messages.isEmpty()) {
            log.warn("txt에서 메시지를 한 건도 읽지 못함");
            throw new BusinessException(ErrorCode.INVALID_TXT_FORMAT);
        }
        return messages;
    }

    private List<RawMessage> readMessages(Reader reader) {
        MessageAssembler assembler = new MessageAssembler();

        try (BufferedReader lines = new BufferedReader(reader)) {
            for (String line = removeBom(lines.readLine()); line != null; line = lines.readLine()) {
                parseLine(assembler, line);
            }
        } catch (IOException e) {
            log.error("txt 읽기 실패", e);
            throw new BusinessException(ErrorCode.FILE_READ_FAILED, e);
        }

        return assembler.complete();
    }

    private void parseLine(MessageAssembler assembler, String line) {
        Matcher messageStart = MESSAGE_START_LINE.matcher(line);

        if (messageStart.matches()) {
            PendingMessage message = toPendingMessage(messageStart);
            assembler.startNewMessage(message);
            return;
        }
        if (isDateDivider(line)) {
            return;
        }
        assembler.appendLine(line);
    }

    private PendingMessage toPendingMessage(Matcher messageStart) {
        return new PendingMessage(
                messageStart.group("speakerName"),
                parseSentAt(messageStart.group("sentAt")),
                messageStart.group("text"));
    }

    private LocalDateTime parseSentAt(String raw) {
        try {
            return LocalDateTime.parse(raw, SENT_AT_FORMAT);
        } catch (DateTimeParseException e) {
            log.warn("txt 시각 변환 실패");
            throw new BusinessException(ErrorCode.INVALID_TXT_FORMAT, e);
        }
    }

    private boolean isDateDivider(String line) {
        return DATE_DIVIDER_LINE.matcher(line).matches();
    }

    private String removeBom(String firstLine) {
        if (firstLine == null || !firstLine.startsWith(BOM)) {
            return firstLine;
        }
        return firstLine.substring(BOM.length());
    }
}
