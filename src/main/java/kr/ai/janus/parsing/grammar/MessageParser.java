package kr.ai.janus.parsing.grammar;

import java.io.Reader;
import java.util.List;

import kr.ai.janus.parsing.model.RawMessage;

/**
 * 카카오톡에서 내보낸 대화 파일을 읽어 RawMessage 목록으로 만든다.
 */
public interface MessageParser {

    boolean supports(String fileName);
    List<RawMessage> parse(Reader reader);
}
