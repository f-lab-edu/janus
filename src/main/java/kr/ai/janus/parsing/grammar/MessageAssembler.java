package kr.ai.janus.parsing.grammar;

import java.util.ArrayList;
import java.util.List;

import kr.ai.janus.parsing.model.RawMessage;

/**
 * 여러 줄 메시지를 RawMessage 목록으로 조립한다.
 *
 * <p>다음 메시지가 시작되거나 파싱이 끝날 때
 * 현재 메시지를 결과 목록에 추가한다.
 */
final class MessageAssembler {

    private final List<RawMessage> messages = new ArrayList<>();
    private PendingMessage pending;

    void startNewMessage(PendingMessage message) {
        saveCurrentMessage();
        pending = message;
    }

    void appendLine(String line) {
        if (pending != null) {
            pending.appendLine(line);
        }
    }

    List<RawMessage> complete() {
        saveCurrentMessage();
        return List.copyOf(messages);
    }

    private void saveCurrentMessage() {
        if (pending == null) {
            return;
        }
        messages.add(pending.toRawMessage());
        pending = null;
    }
}
