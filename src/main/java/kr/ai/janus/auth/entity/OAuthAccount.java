package kr.ai.janus.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

import kr.ai.janus.auth.OAuthProvider;
import kr.ai.janus.common.BaseTimeEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "oauth_account")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OAuthAccount extends BaseTimeEntity {

    @EmbeddedId
    private OAuthAccountId id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private LocalDateTime lastLoginAt;

    private OAuthAccount(OAuthAccountId id, Long userId, LocalDateTime lastLoginAt) {
        this.id = id;
        this.userId = userId;
        this.lastLoginAt = lastLoginAt;
    }

    public static OAuthAccount register(OAuthProvider provider, String providerSubject, Long userId, LocalDateTime loginAt) {
        return new OAuthAccount(OAuthAccountId.of(provider, providerSubject), userId, loginAt);
    }

    public void recordLogin(LocalDateTime at) {
        this.lastLoginAt = at;
    }
}
