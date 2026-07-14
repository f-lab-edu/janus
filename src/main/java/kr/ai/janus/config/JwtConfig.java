package kr.ai.janus.config;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.KeyLengthException;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import java.util.Base64;
import kr.ai.janus.auth.JwtProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtConfig {

    @Bean
    public MACSigner jwtSigner(JwtProperties properties) {
        try {
            return new MACSigner(decodeSecret(properties));
        } catch (KeyLengthException e) {
            throw new IllegalStateException("JWT secret 길이가 부족합니다(HS256은 256비트 이상).", e);
        }
    }

    @Bean
    public MACVerifier jwtVerifier(JwtProperties properties) {
        try {
            return new MACVerifier(decodeSecret(properties));
        } catch (JOSEException e) {
            throw new IllegalStateException("JWT 검증기 생성에 실패했습니다.", e);
        }
    }

    private byte[] decodeSecret(JwtProperties properties) {
        try {
            return Base64.getDecoder().decode(properties.secret());
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("JWT secret이 올바른 Base64 형식이 아닙니다.", e);
        }
    }
}
