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
    REJOIN_RESTRICTED(HttpStatus.CONFLICT, "탈퇴 후 24시간이 지나야 다시 가입할 수 있습니다."),
    SIGNUP_CONFLICT(HttpStatus.INTERNAL_SERVER_ERROR, "가입 처리 중 문제가 발생했습니다."),

    // 카카오 로그인 관련
    KAKAO_TOKEN_EXCHANGE_FAILED(HttpStatus.BAD_GATEWAY, "카카오 로그인에 실패했습니다."),
    KAKAO_PROFILE_FETCH_FAILED(HttpStatus.BAD_GATEWAY, "카카오 사용자 정보 조회에 실패했습니다."),

    // 공통
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "요청이 올바르지 않습니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
