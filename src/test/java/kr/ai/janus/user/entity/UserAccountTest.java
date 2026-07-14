package kr.ai.janus.user.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import kr.ai.janus.common.exception.BusinessException;
import kr.ai.janus.common.exception.ErrorCode;
import kr.ai.janus.user.UserStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserAccountTest {

    @Test
    @DisplayName("ACTIVE 유저는 로그인할 수 있다")
    void activeCanLogin() {
        UserAccount user = UserAccount.create();

        assertThatCode(user::onLogin).doesNotThrowAnyException();
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    @DisplayName("차단된 유저는 INACTIVE_USER로 거부된다")
    void blockedCannotLogin() {
        UserAccount user = UserAccount.create();
        user.block(LocalDateTime.parse("2026-01-01T00:00:00"));

        assertThatThrownBy(user::onLogin)
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.INACTIVE_USER));
    }
}
