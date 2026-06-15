package kr.ai.janus.auth.service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.KeyLengthException;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import kr.ai.janus.auth.JwtProperties;
import kr.ai.janus.user.entity.UserAccount;
import org.springframework.stereotype.Component;

@Component
public class TokenIssuer {

    private static final JWSAlgorithm ALGORITHM = JWSAlgorithm.HS256;

    private final JWSSigner signer;
    private final Duration validity;
    private final Clock clock;

    public TokenIssuer(JwtProperties properties, Clock clock) {
        this.signer = createSigner(properties.secret());
        this.validity = properties.accessTokenValidity();
        this.clock = clock;
    }

    public String issue(UserAccount user) {
        Instant now = clock.instant();
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(String.valueOf(user.getId()))
                .issueTime(Date.from(now))
                .expirationTime(Date.from(now.plus(validity)))
                .build();
        SignedJWT jwt = new SignedJWT(new JWSHeader(ALGORITHM), claims);
        try {
            jwt.sign(signer);
        } catch (JOSEException e) {
            throw new IllegalStateException("JWT 서명에 실패했습니다.", e);
        }
        return jwt.serialize();
    }

    private JWSSigner createSigner(String secret) {
        try {
            return new MACSigner(Base64.getDecoder().decode(secret));
        } catch (KeyLengthException e) {
            throw new IllegalStateException("JWT secret 길이가 부족합니다.", e);
        }
    }
}
