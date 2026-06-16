package kr.ai.janus.auth.controller;

import jakarta.validation.Valid;
import kr.ai.janus.auth.dto.KakaoLoginRequest;
import kr.ai.janus.auth.dto.MeResponse;
import kr.ai.janus.auth.dto.TokenResponse;
import kr.ai.janus.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/auth/kakao")
    public TokenResponse login(@Valid @RequestBody KakaoLoginRequest request) {
        return authService.loginWithKakao(request.code());
    }

    @GetMapping("/auth/me")
    public MeResponse getCurrentUser(@AuthenticationPrincipal Long userId) {
        return new MeResponse(userId);
    }
}
