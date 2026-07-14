package kr.ai.janus.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 로그인 관련
    OAUTH_USER_MISSING(HttpStatus.INTERNAL_SERVER_ERROR, "계정 정보가 올바르지 않습니다."),
    INACTIVE_USER(HttpStatus.FORBIDDEN, "로그인할 수 없는 계정입니다."),
    SIGNUP_CONFLICT(HttpStatus.CONFLICT, "로그인 요청이 겹쳤습니다. 다시 시도해주세요."),

    // 카카오 로그인 관련
    KAKAO_TOKEN_EXCHANGE_FAILED(HttpStatus.BAD_GATEWAY, "카카오 로그인에 실패했습니다."),
    KAKAO_PROFILE_FETCH_FAILED(HttpStatus.BAD_GATEWAY, "카카오 사용자 정보 조회에 실패했습니다."),

    // 공통
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "요청이 올바르지 않습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
