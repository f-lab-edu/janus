package kr.ai.janus.auth.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Date;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TokenVerifierTest {

    private static final byte[] KEY = new byte[32];
    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    private final Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);

    private TokenVerifier verifier() throws Exception {
        return new TokenVerifier(new MACVerifier(KEY), clock);
    }

    private String token(long userId, Instant expiration, byte[] signingKey) throws Exception {
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(String.valueOf(userId))
                .expirationTime(Date.from(expiration))
                .build();
        SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
        jwt.sign(new MACSigner(signingKey));
        return jwt.serialize();
    }

    @Test
    @DisplayName("유효한 토큰이면 userId를 돌려준다")
    void validToken() throws Exception {
        String token = token(42L, NOW.plus(Duration.ofMinutes(30)), KEY);

        assertThat(verifier().parseUserId(token)).contains(42L);
    }

    @Test
    @DisplayName("만료된 토큰이면 비어 있다")
    void expiredToken() throws Exception {
        String token = token(42L, NOW.minusSeconds(1), KEY);

        assertThat(verifier().parseUserId(token)).isEmpty();
    }

    @Test
    @DisplayName("서명이 다른 키면 비어 있다")
    void wrongSignature() throws Exception {
        byte[] otherKey = new byte[32];
        otherKey[0] = 1;
        String token = token(42L, NOW.plus(Duration.ofMinutes(30)), otherKey);

        assertThat(verifier().parseUserId(token)).isEmpty();
    }

    @Test
    @DisplayName("깨진 토큰이면 비어 있다")
    void malformedToken() throws Exception {
        assertThat(verifier().parseUserId("not-a-jwt")).isEmpty();
    }
}
