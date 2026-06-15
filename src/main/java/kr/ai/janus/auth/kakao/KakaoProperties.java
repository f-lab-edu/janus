package kr.ai.janus.auth.kakao;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kakao")
public record KakaoProperties(
        String restApiKey,
        String clientSecret,
        String redirectUri,
        String authUrl,
        String apiUrl
) {
}
