package kr.ai.janus.parsing.grammar;

import java.time.LocalDateTime;

import kr.ai.janus.parsing.model.RawMessage;

final class PendingMessage {

    private static final String LINE_BREAK = "\n";

    private final String speakerName;
    private final LocalDateTime sentAt;
    private final StringBuilder text;

    PendingMessage(String speakerName, LocalDateTime sentAt, String firstLine) {
        this.speakerName = speakerName;
        this.sentAt = sentAt;
        this.text = new StringBuilder(firstLine);
    }

    void appendLine(String line) {
        text.append(LINE_BREAK).append(line);
    }

    RawMessage toRawMessage() {
        return new RawMessage(speakerName, sentAt, text.toString());
    }
}
