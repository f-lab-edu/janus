package kr.ai.janus.auth.kakao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import kr.ai.janus.common.exception.BusinessException;
import kr.ai.janus.common.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class KakaoClientTest {

    private static final String AUTH_URL = "https://kauth.kakao.com";
    private static final String API_URL = "https://kapi.kakao.com";

    private MockRestServiceServer server;
    private KakaoClient kakaoClient;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        KakaoProperties properties = new KakaoProperties(
                "rest-key", "client-secret", "http://localhost/callback", AUTH_URL, API_URL);
        kakaoClient = new KakaoClient(builder.build(), properties);
    }

    @Test
    @DisplayName("토큰 교환 후 사용자 정보로 subject를 반환한다")
    void fetchProfileReturnsSubject() {
        server.expect(requestTo(AUTH_URL + "/oauth/token"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{\"access_token\":\"at-123\"}", MediaType.APPLICATION_JSON));
        server.expect(requestTo(API_URL + "/v2/user/me"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer at-123"))
                .andRespond(withSuccess("{\"id\":42}", MediaType.APPLICATION_JSON));

        KakaoProfile profile = kakaoClient.fetchProfile("auth-code");

        assertThat(profile.subject()).isEqualTo("42");
        server.verify();
    }

    @Test
    @DisplayName("토큰 교환이 실패하면 KAKAO_TOKEN_EXCHANGE_FAILED를 던진다")
    void tokenExchangeError() {
        server.expect(requestTo(AUTH_URL + "/oauth/token"))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST));

        assertThatThrownBy(() -> kakaoClient.fetchProfile("bad-code"))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.KAKAO_TOKEN_EXCHANGE_FAILED));
    }

    @Test
    @DisplayName("토큰 응답에 access_token이 없으면 KAKAO_TOKEN_EXCHANGE_FAILED를 던진다")
    void tokenResponseMissingAccessToken() {
        server.expect(requestTo(AUTH_URL + "/oauth/token"))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> kakaoClient.fetchProfile("auth-code"))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.KAKAO_TOKEN_EXCHANGE_FAILED));
    }

    @Test
    @DisplayName("사용자 정보 조회가 실패하면 KAKAO_PROFILE_FETCH_FAILED를 던진다")
    void userFetchError() {
        server.expect(requestTo(AUTH_URL + "/oauth/token"))
                .andRespond(withSuccess("{\"access_token\":\"at-123\"}", MediaType.APPLICATION_JSON));
        server.expect(requestTo(API_URL + "/v2/user/me"))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED));

        assertThatThrownBy(() -> kakaoClient.fetchProfile("auth-code"))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.KAKAO_PROFILE_FETCH_FAILED));
    }
}
