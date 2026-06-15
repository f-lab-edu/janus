package kr.ai.janus.auth.service;

import kr.ai.janus.auth.OAuthProvider;
import kr.ai.janus.auth.dto.TokenResponse;
import kr.ai.janus.auth.kakao.KakaoClient;
import kr.ai.janus.auth.kakao.KakaoProfile;
import kr.ai.janus.user.entity.UserAccount;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final KakaoClient kakaoClient;
    private final UserRegistration userRegistration;
    private final TokenIssuer tokenIssuer;

    public TokenResponse loginWithKakao(String code) {
        KakaoProfile profile = kakaoClient.fetchProfile(code);
        UserAccount user = loginOrSignupWithRetry(OAuthProvider.KAKAO, profile.subject());
        return new TokenResponse(tokenIssuer.issue(user));
    }

    private UserAccount loginOrSignupWithRetry(OAuthProvider provider, String subject) {
        try {
            return userRegistration.loginOrSignup(provider, subject);
        } catch (DataIntegrityViolationException e) {
            // 동시 가입 PK 충돌이면 먼저 가입된 계정으로 로그인하고,
            // 없으면 다른 제약 위반이므로 원인을 그대로 남긴다.
            return userRegistration.loginExisting(provider, subject, e);
        }
    }
}
