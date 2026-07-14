package kr.ai.janus.auth.service;

import java.time.Clock;
import java.time.LocalDateTime;

import kr.ai.janus.auth.OAuthProvider;
import kr.ai.janus.auth.entity.OAuthAccount;
import kr.ai.janus.auth.entity.OAuthAccountId;
import kr.ai.janus.auth.repository.OAuthAccountRepository;
import kr.ai.janus.common.exception.BusinessException;
import kr.ai.janus.common.exception.ErrorCode;
import kr.ai.janus.user.entity.UserAccount;
import kr.ai.janus.user.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserRegistration {

    private final UserAccountRepository userAccountRepository;
    private final OAuthAccountRepository oauthAccountRepository;
    private final Clock clock;

    @Transactional
    public UserAccount loginOrSignup(OAuthProvider provider, String subject) {
        return oauthAccountRepository.findById(OAuthAccountId.of(provider, subject))
                .map(this::login)
                .orElseGet(() -> signup(provider, subject));
    }

    private UserAccount login(OAuthAccount oauth) {
        UserAccount user = loadUser(oauth.getUserId());
        user.onLogin();
        oauth.recordLogin(LocalDateTime.now(clock));
        return user;
    }

    private UserAccount signup(OAuthProvider provider, String subject) {
        UserAccount user = userAccountRepository.save(UserAccount.create());
        oauthAccountRepository.save(
                OAuthAccount.register(provider, subject, user.getId(), LocalDateTime.now(clock)));
        return user;
    }

    private UserAccount loadUser(Long userId) {
        return userAccountRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.OAUTH_USER_MISSING));
    }
}
