package kr.ai.janus.auth.service;

import kr.ai.janus.auth.OAuthProvider;
import kr.ai.janus.auth.dto.TokenResponse;
import kr.ai.janus.auth.kakao.KakaoClient;
import kr.ai.janus.auth.kakao.KakaoProfile;
import kr.ai.janus.common.exception.BusinessException;
import kr.ai.janus.common.exception.ErrorCode;
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
        UserAccount user = loginOrSignup(OAuthProvider.KAKAO, profile.subject());
        return new TokenResponse(tokenIssuer.issue(user));
    }

    private UserAccount loginOrSignup(OAuthProvider provider, String subject) {
        try {
            return userRegistration.loginOrSignup(provider, subject);
        } catch (DataIntegrityViolationException e) {
            // 동시 첫 로그인 경쟁에서 진 요청 — 계정 정합성은 PK가 보장하므로 재시도만 안내한다
            throw new BusinessException(ErrorCode.SIGNUP_CONFLICT, e);
        }
    }
}
