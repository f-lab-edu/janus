package kr.ai.janus.common.exception;

import java.time.Instant;
import lombok.Getter;

@Getter
public class RejoinRestrictedException extends BusinessException {

    private final Instant availableAt;

    public RejoinRestrictedException(Instant availableAt) {
        super(ErrorCode.REJOIN_RESTRICTED);
        this.availableAt = availableAt;
    }
}
