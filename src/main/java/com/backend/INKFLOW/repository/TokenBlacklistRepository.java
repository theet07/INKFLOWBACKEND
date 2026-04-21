package com.backend.INKFLOW.repository;

import com.backend.INKFLOW.model.TokenBlacklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;

@Repository
public interface TokenBlacklistRepository extends JpaRepository<TokenBlacklist, Long> {
    boolean existsByTokenId(String tokenId);
    void deleteByExpiresAtBefore(LocalDateTime dateTime);
}
