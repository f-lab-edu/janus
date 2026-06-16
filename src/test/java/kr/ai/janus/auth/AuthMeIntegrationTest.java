package kr.ai.janus.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import kr.ai.janus.auth.service.TokenIssuer;
import kr.ai.janus.support.IntegrationTest;
import kr.ai.janus.user.entity.UserAccount;
import kr.ai.janus.user.repository.UserAccountRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@AutoConfigureMockMvc
@Transactional
class AuthMeIntegrationTest extends IntegrationTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    TokenIssuer tokenIssuer;
    @Autowired
    UserAccountRepository userAccountRepository;

    @Test
    @DisplayName("유효한 토큰으로 /auth/me 호출 시 userId를 반환한다")
    void meWithValidToken() throws Exception {
        UserAccount user = userAccountRepository.save(UserAccount.create());
        String token = tokenIssuer.issue(user);

        mockMvc.perform(get("/auth/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(user.getId().intValue()));
    }

    @Test
    @DisplayName("잘못된 토큰이면 /auth/me는 401을 반환한다")
    void meWithGarbageToken() throws Exception {
        mockMvc.perform(get("/auth/me").header(HttpHeaders.AUTHORIZATION, "Bearer garbage"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }
}
