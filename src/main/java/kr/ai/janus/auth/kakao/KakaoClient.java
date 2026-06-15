package kr.ai.janus.auth.kakao;

import kr.ai.janus.common.exception.BusinessException;
import kr.ai.janus.common.exception.ErrorCode;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Component
public class KakaoClient {

    private final RestClient restClient;
    private final KakaoProperties properties;

    public KakaoClient(RestClient kakaoRestClient, KakaoProperties properties) {
        this.restClient = kakaoRestClient;
        this.properties = properties;
    }

    public KakaoProfile fetchProfile(String code) {
        String accessToken = exchangeToken(code);
        return fetchUser(accessToken);
    }

    private String exchangeToken(String code) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("client_id", properties.restApiKey());
        form.add("client_secret", properties.clientSecret());
        form.add("redirect_uri", properties.redirectUri());
        form.add("code", code);

        KakaoTokenResponse response = restClient.post()
                .uri(properties.authUrl() + "/oauth/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, res) -> {
                    throw new BusinessException(ErrorCode.KAKAO_TOKEN_EXCHANGE_FAILED);
                })
                .body(KakaoTokenResponse.class);

        if (response == null || response.accessToken() == null) {
            throw new BusinessException(ErrorCode.KAKAO_TOKEN_EXCHANGE_FAILED);
        }
        return response.accessToken();
    }

    private KakaoProfile fetchUser(String accessToken) {
        KakaoUserResponse response = restClient.get()
                .uri(properties.apiUrl() + "/v2/user/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, res) -> {
                    throw new BusinessException(ErrorCode.KAKAO_PROFILE_FETCH_FAILED);
                })
                .body(KakaoUserResponse.class);

        if (response == null || response.id() == null) {
            throw new BusinessException(ErrorCode.KAKAO_PROFILE_FETCH_FAILED);
        }
        return new KakaoProfile(String.valueOf(response.id()));
    }
}
