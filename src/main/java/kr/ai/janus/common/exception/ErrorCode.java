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

    // 파싱 관련
    INVALID_CSV_FORMAT(HttpStatus.BAD_REQUEST, "파일 분석에 실패했습니다. 파일을 확인 후 다시 시도해주세요."),
    INVALID_TXT_FORMAT(HttpStatus.BAD_REQUEST,
            "대화 파일을 분석할 수 없습니다. 현재는 한국어 환경에서 내보낸 카카오톡 대화 파일만 지원합니다."),
    UNSUPPORTED_FILE_FORMAT(HttpStatus.BAD_REQUEST, "지원하지 않는 파일 형식입니다."),
    FILE_READ_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "파일을 읽는 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요."),
    INSUFFICIENT_TOTAL_MESSAGES(HttpStatus.BAD_REQUEST, "분석에 필요한 전체 메시지 수가 부족합니다."),
    INSUFFICIENT_SPEAKERS(HttpStatus.BAD_REQUEST, "대화 상대가 없어요. 두 사람이 주고받은 대화를 올려주세요."),
    NOT_ONE_TO_ONE_CHAT(HttpStatus.BAD_REQUEST, "1:1 대화만 분석할 수 있어요. 두 사람만의 대화를 올려주세요."),
    INSUFFICIENT_MESSAGES_PER_SPEAKER(HttpStatus.BAD_REQUEST, "두 사람 중 한 명의 메시지 수가 부족합니다."),
    UNKNOWN_SPEAKER(HttpStatus.BAD_REQUEST, "선택한 이름을 대화에서 찾을 수 없습니다."),

    // 공통
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "요청이 올바르지 않습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
