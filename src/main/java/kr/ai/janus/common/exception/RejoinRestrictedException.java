package kr.ai.janus.common.exception;

import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class RejoinRestrictedException extends BusinessException {

    private final LocalDateTime availableAt;

    public RejoinRestrictedException(LocalDateTime availableAt) {
        super(ErrorCode.REJOIN_RESTRICTED);
        this.availableAt = availableAt;
    }
}
