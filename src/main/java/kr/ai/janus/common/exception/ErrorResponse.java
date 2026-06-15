package kr.ai.janus.common.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL) // null인 필드는 JSON 응답에서 제외
public record ErrorResponse(
        String code,
        String message,
        List<FieldError> fieldErrors,
        Instant availableAt) {

    public record FieldError(String field, String reason) {
    }

    public static ErrorResponse from(ErrorCode errorCode) {
        return new ErrorResponse(errorCode.name(), errorCode.getMessage(), null, null);
    }

    public static ErrorResponse from(ErrorCode errorCode, List<FieldError> fieldErrors) {
        return new ErrorResponse(errorCode.name(), errorCode.getMessage(), fieldErrors, null);
    }

    public static ErrorResponse from(ErrorCode errorCode, Instant availableAt) {
        return new ErrorResponse(errorCode.name(), errorCode.getMessage(), null, availableAt);
    }
}
