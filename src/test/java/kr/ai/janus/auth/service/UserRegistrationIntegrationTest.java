package kr.ai.janus.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import kr.ai.janus.auth.OAuthProvider;
import kr.ai.janus.auth.entity.OAuthAccount;
import kr.ai.janus.auth.entity.OAuthAccountId;
import kr.ai.janus.auth.repository.OAuthAccountRepository;
import kr.ai.janus.common.exception.BusinessException;
import kr.ai.janus.common.exception.ErrorCode;
import kr.ai.janus.support.IntegrationTest;
import kr.ai.janus.user.UserStatus;
import kr.ai.janus.user.entity.UserAccount;
import kr.ai.janus.user.repository.UserAccountRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class UserRegistrationIntegrationTest extends IntegrationTest {

    @Autowired
    UserRegistration userRegistration;
    @Autowired
    UserAccountRepository userAccountRepository;
    @Autowired
    OAuthAccountRepository oauthAccountRepository;
    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("신규 가입이면 ACTIVE user와 oauth가 생성된다")
    void signupCreatesActiveUser() {
        UserAccount user = userRegistration.loginOrSignup(OAuthProvider.KAKAO, "new-1");

        assertThat(user.getId()).isNotNull();
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(oauthAccountRepository.findById(OAuthAccountId.of(OAuthProvider.KAKAO, "new-1"))).isPresent();
    }

    @Test
    @DisplayName("기존 계정이면 같은 user로 로그인된다")
    void existingAccountLogsInWithSameUser() {
        UserAccount first = userRegistration.loginOrSignup(OAuthProvider.KAKAO, "existing-1");
        UserAccount second = userRegistration.loginOrSignup(OAuthProvider.KAKAO, "existing-1");

        assertThat(second.getId()).isEqualTo(first.getId());
        assertThat(userAccountRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("로그인 경로가 onLogin 상태검사를 실제로 태운다 (차단 유저 거부로 확인)")
    void loginPathRunsOnLoginCheck() {
        UserAccount user = userRegistration.loginOrSignup(OAuthProvider.KAKAO, "blocked-1");
        user.block(Instant.now());
        userAccountRepository.save(user);

        assertThatThrownBy(() -> userRegistration.loginOrSignup(OAuthProvider.KAKAO, "blocked-1"))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.INACTIVE_USER));
    }

    @Test
    @DisplayName("타임스탬프는 UTC로 저장된다")
    void timestampIsStoredAsUtc() {
        Instant fixed = Instant.parse("2026-01-01T00:00:00Z");
        UserAccount user = userAccountRepository.save(UserAccount.create());
        oauthAccountRepository.saveAndFlush(
                OAuthAccount.register(OAuthProvider.KAKAO, "tz-sub", user.getId(), fixed));

        String raw = jdbcTemplate.queryForObject(
                "select cast(last_login_at as char) from oauth_account where provider_subject = ?",
                String.class, "tz-sub");

        assertThat(raw).startsWith("2026-01-01 00:00:00");
    }
}
