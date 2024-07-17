package kr.sesacjava.swimtutor.oauth.repository;

import kr.sesacjava.swimtutor.oauth.domain.OAuthLoginId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OAuthLoginRepository extends JpaRepository<OAuthLoginRepository, OAuthLoginId> {
}
