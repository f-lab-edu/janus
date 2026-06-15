package kr.ai.janus.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Duration;
import java.time.Instant;
import kr.ai.janus.common.BaseTimeEntity;
import kr.ai.janus.common.exception.BusinessException;
import kr.ai.janus.common.exception.ErrorCode;
import kr.ai.janus.common.exception.RejoinRestrictedException;
import kr.ai.janus.user.UserStatus;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_account")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserAccount extends BaseTimeEntity {

    private static final Duration REJOIN_RESTRICTION_PERIOD = Duration.ofHours(24);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 40)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private UserStatus status;

    private Instant blockedAt;

    private Instant withdrawnAt;

    private UserAccount(UserStatus status) {
        this.status = status;
    }

    public static UserAccount create() {
        return new UserAccount(UserStatus.ACTIVE);
    }

    public void onLogin(Instant now) {
        if (status == UserStatus.WITHDRAWN) {
            reactivateAfterRestriction(now);
            return;
        }
        if (status != UserStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.INACTIVE_USER);
        }
    }

    private void reactivateAfterRestriction(Instant now) {
        Instant availableAt = withdrawnAt.plus(REJOIN_RESTRICTION_PERIOD);
        if (now.isBefore(availableAt)) {
            throw new RejoinRestrictedException(availableAt);
        }
        reactivate();
    }

    private void reactivate() {
        this.status = UserStatus.ACTIVE;
        this.withdrawnAt = null;
    }

    public void block(Instant at) {
        this.status = UserStatus.BLOCKED;
        this.blockedAt = at;
    }

    public void withdraw(Instant at) {
        this.status = UserStatus.WITHDRAWN;
        this.withdrawnAt = at;
    }
}
