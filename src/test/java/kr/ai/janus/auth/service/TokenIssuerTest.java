package kr.ai.janus.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.SignedJWT;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.Date;
import kr.ai.janus.auth.JwtProperties;
import kr.ai.janus.user.entity.UserAccount;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TokenIssuerTest {

    private static final String SECRET = Base64.getEncoder().encodeToString(new byte[32]);

    @Test
    @DisplayName("발급한 토큰은 subject가 userId이고 exp가 발급시각+유효기간이며 서명이 유효하다")
    void issueToken() throws Exception {
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        Clock clock = Clock.fixed(now, ZoneOffset.UTC);
        Duration validity = Duration.ofHours(1);
        TokenIssuer tokenIssuer = new TokenIssuer(new JwtProperties(SECRET, validity), clock);

        UserAccount user = mock(UserAccount.class);
        given(user.getId()).willReturn(42L);

        String token = tokenIssuer.issue(user);

        SignedJWT jwt = SignedJWT.parse(token);
        assertThat(jwt.verify(new MACVerifier(Base64.getDecoder().decode(SECRET)))).isTrue();
        assertThat(jwt.getJWTClaimsSet().getSubject()).isEqualTo("42");
        assertThat(jwt.getJWTClaimsSet().getIssueTime()).isEqualTo(Date.from(now));
        assertThat(jwt.getJWTClaimsSet().getExpirationTime()).isEqualTo(Date.from(now.plus(validity)));
    }
}
