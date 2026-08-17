package kr.ai.janus.parsing.grammar;

import java.util.List;

import org.springframework.stereotype.Component;

import kr.ai.janus.common.exception.BusinessException;
import kr.ai.janus.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 파일 이름을 보고 그 파일을 읽을 파서를 고른다.
 * 파일 이름 자체에 대화방 이름이 들어 있으므로 로그에는 확장자만 남긴다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public final class MessageParserSelector {

    private static final String NO_EXTENSION = "(없음)";

    private final List<MessageParser> parsers;

    public MessageParser select(String fileName) {
        if (hasNoFileName(fileName)) {
            log.error("파일 이름 없는 업로드");
            throw new BusinessException(ErrorCode.UNSUPPORTED_FILE_FORMAT);
        }

        return parsers.stream()
                .filter(parser -> parser.supports(fileName))
                .findFirst()
                .orElseThrow(() -> unsupportedFileFormat(fileName));
    }

    private boolean hasNoFileName(String fileName) {
        return fileName == null || fileName.isBlank();
    }

    private BusinessException unsupportedFileFormat(String fileName) {
        log.warn("지원하지 않는 확장자: {}", extensionOf(fileName));
        return new BusinessException(ErrorCode.UNSUPPORTED_FILE_FORMAT);
    }

    private String extensionOf(String fileName) {
        int lastDot = fileName.lastIndexOf('.');

        if (lastDot < 0 || lastDot == fileName.length() - 1) {
            return NO_EXTENSION;
        }

        return fileName.substring(lastDot);
    }
}
