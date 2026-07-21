package kr.ai.janus.auth.service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.text.ParseException;
import java.time.Clock;
import java.util.Date;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class TokenVerifier {

    private final JWSVerifier verifier;
    private final Clock clock;

    public TokenVerifier(MACVerifier jwtVerifier, Clock clock) {
        this.verifier = jwtVerifier;
        this.clock = clock;
    }

    public Optional<Long> parseUserId(String token) {
        try {
            return verifyAndExtract(token);
        } catch (ParseException | JOSEException | NumberFormatException e) {
            return Optional.empty();
        }
    }

    private Optional<Long> verifyAndExtract(String token) throws ParseException, JOSEException {
        SignedJWT jwt = SignedJWT.parse(token);
        if (!jwt.verify(verifier)) {
            return Optional.empty();
        }
        JWTClaimsSet claims = jwt.getJWTClaimsSet();
        if (isExpired(claims)) {
            return Optional.empty();
        }
        return Optional.of(Long.valueOf(claims.getSubject()));
    }

    private boolean isExpired(JWTClaimsSet claims) {
        Date expiration = claims.getExpirationTime();
        return expiration == null || !clock.instant().isBefore(expiration.toInstant());
    }
}
