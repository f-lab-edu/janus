package kr.ai.janus.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.io.Serializable;

import kr.ai.janus.auth.OAuthProvider;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OAuthAccountId implements Serializable {

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private OAuthProvider provider;

    @Column(length = 64)
    private String providerSubject;

    private OAuthAccountId(OAuthProvider provider, String providerSubject) {
        this.provider = provider;
        this.providerSubject = providerSubject;
    }

    public static OAuthAccountId of(OAuthProvider provider, String providerSubject) {
        return new OAuthAccountId(provider, providerSubject);
    }
}
