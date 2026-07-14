package kr.ai.janus.user.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.time.LocalDateTime;
import kr.ai.janus.common.exception.BusinessException;
import kr.ai.janus.common.exception.ErrorCode;
import kr.ai.janus.common.exception.RejoinRestrictedException;
import kr.ai.janus.user.UserStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserAccountTest {

    private static final LocalDateTime WITHDRAWN_AT = LocalDateTime.parse("2026-01-01T00:00:00");

    @Test
    @DisplayName("ACTIVE 유저는 로그인할 수 있다")
    void activeCanLogin() {
        UserAccount user = UserAccount.create();

        assertThatCode(() -> user.onLogin(LocalDateTime.parse("2026-06-01T00:00:00")))
                .doesNotThrowAnyException();
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    @DisplayName("차단된 유저는 INACTIVE_USER로 거부된다")
    void blockedCannotLogin() {
        UserAccount user = UserAccount.create();
        user.block(WITHDRAWN_AT);

        assertThatThrownBy(() -> user.onLogin(LocalDateTime.parse("2026-06-01T00:00:00")))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.INACTIVE_USER));
    }

    @Test
    @DisplayName("탈퇴 후 24시간 이내 재로그인은 재가입 제한으로 거부되고 availableAt을 알려준다")
    void withdrawnWithinRestrictionRejected() {
        UserAccount user = UserAccount.create();
        user.withdraw(WITHDRAWN_AT);
        LocalDateTime now = WITHDRAWN_AT.plus(Duration.ofHours(23));

        assertThatThrownBy(() -> user.onLogin(now))
                .isInstanceOfSatisfying(RejoinRestrictedException.class, e -> {
                    assertThat(e.getErrorCode()).isEqualTo(ErrorCode.REJOIN_RESTRICTED);
                    assertThat(e.getAvailableAt()).isEqualTo(WITHDRAWN_AT.plus(Duration.ofHours(24)));
                });
        assertThat(user.getStatus()).isEqualTo(UserStatus.WITHDRAWN);
    }

    @Test
    @DisplayName("탈퇴 후 24시간이 지나면 재로그인 시 재활성화된다")
    void withdrawnAfterCooldownReactivated() {
        UserAccount user = UserAccount.create();
        user.withdraw(WITHDRAWN_AT);
        LocalDateTime now = WITHDRAWN_AT.plus(Duration.ofHours(24));

        user.onLogin(now);

        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(user.getWithdrawnAt()).isNull();
    }
}
