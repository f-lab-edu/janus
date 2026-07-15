package kr.ai.janus.auth.repository;

import kr.ai.janus.auth.entity.OAuthAccount;
import kr.ai.janus.auth.entity.OAuthAccountId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OAuthAccountRepository extends JpaRepository<OAuthAccount, OAuthAccountId> {
}
