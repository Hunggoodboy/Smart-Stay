package vn.edu.ptit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.ptit.entity.RefreshToken;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    Optional<RefreshToken> findByToken(String refreshToken);
    void deleteByToken(String refreshToken);
}
