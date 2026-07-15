package kr.ai.janus.auth.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;
import kr.ai.janus.auth.dto.TokenResponse;
import kr.ai.janus.auth.service.AuthService;
import kr.ai.janus.auth.service.TokenVerifier;
import kr.ai.janus.config.CorsProperties;
import kr.ai.janus.config.SecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
@EnableConfigurationProperties(CorsProperties.class)
class AuthControllerTest {

    @Autowired
    MockMvc mockMvc;
    @MockitoBean
    AuthService authService;
    @MockitoBean
    TokenVerifier tokenVerifier;

    @Test
    @DisplayName("유효한 요청이면 200과 accessToken을 반환한다")
    void login() throws Exception {
        given(authService.loginWithKakao("auth-code")).willReturn(new TokenResponse("jwt-token"));

        mockMvc.perform(post("/auth/kakao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"auth-code\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("jwt-token"));
    }

    @Test
    @DisplayName("code가 비어 있으면 400을 반환하고 서비스를 호출하지 않는다")
    void blankCode() throws Exception {
        mockMvc.perform(post("/auth/kakao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("code"));

        then(authService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("예상치 못한 예외는 500과 INTERNAL_ERROR로 응답한다")
    void unexpectedError() throws Exception {
        given(authService.loginWithKakao("auth-code")).willThrow(new RuntimeException("boom"));

        mockMvc.perform(post("/auth/kakao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"auth-code\"}"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"));
    }

    @Test
    @DisplayName("본문 JSON이 깨지면 400을 반환한다")
    void malformedBody() throws Exception {
        mockMvc.perform(post("/auth/kakao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("유효한 토큰이면 /auth/me가 userId를 반환한다")
    void me() throws Exception {
        given(tokenVerifier.parseUserId("valid-token")).willReturn(Optional.of(7L));

        mockMvc.perform(get("/auth/me").header(HttpHeaders.AUTHORIZATION, "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(7));
    }

    @Test
    @DisplayName("토큰이 없으면 /auth/me는 401을 반환한다")
    void meWithoutToken() throws Exception {
        mockMvc.perform(get("/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }
}
