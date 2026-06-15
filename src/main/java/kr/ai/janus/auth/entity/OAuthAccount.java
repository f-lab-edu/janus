package kr.ai.janus.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;

import kr.ai.janus.auth.OAuthProvider;
import kr.ai.janus.common.BaseCreatedEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "oauth_account")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OAuthAccount extends BaseCreatedEntity {

    @EmbeddedId
    private OAuthAccountId id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Instant lastLoginAt;

    private OAuthAccount(OAuthAccountId id, Long userId, Instant lastLoginAt) {
        this.id = id;
        this.userId = userId;
        this.lastLoginAt = lastLoginAt;
    }

    public static OAuthAccount register(OAuthProvider provider, String providerSubject, Long userId, Instant loginAt) {
        return new OAuthAccount(OAuthAccountId.of(provider, providerSubject), userId, loginAt);
    }

    public void recordLogin(Instant at) {
        this.lastLoginAt = at;
    }
}
