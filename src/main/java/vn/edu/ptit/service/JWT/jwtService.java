package vn.edu.ptit.service.JWT;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.ptit.Exception.TokenExpiredException;
import vn.edu.ptit.Exception.UserNotFoundException;
import vn.edu.ptit.dto.Response.TokenResponse;
import vn.edu.ptit.dto.Response.UserResponse;
import vn.edu.ptit.entity.RefreshToken;
import vn.edu.ptit.entity.User;
import vn.edu.ptit.repository.RefreshTokenRepository;
import vn.edu.ptit.repository.UserRepository;
import vn.edu.ptit.service.Authentication.CustomerDetailService;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

@Service
@AllArgsConstructor
public class jwtService {
    private final String jwtSecret = "84985f37-a3aa-4137-aa09-44991a9aecf6";
    private final long jwtExpirationMs = 86400000;
    private final Long refreshTokenValiditySeconds = 1209600L;
    private final CustomerDetailService customerDetailService;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private SecretKey getSigningKey() {
        byte[] keyBytes = this.jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
    public String generateAccessToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);
        UserDetails userDetails = customerDetailService.loadUserByUsername(username);
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException("User not found"));
        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("role", userDetails.getAuthorities().iterator().next().getAuthority().substring(5))
                .issuedAt(now)
                .signWith(getSigningKey())
                .expiration(expiryDate)
                .compact();
    }
    private String generateRefreshToken(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException("User not found"));
        RefreshToken refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .expiredAt(LocalDateTime.now().plusSeconds(refreshTokenValiditySeconds))
                .build();
        refreshTokenRepository.save(refreshToken);
        return refreshToken.getToken();
    }
    public TokenResponse generateToken(String username) {
        return TokenResponse.builder()
                .accessToken(generateAccessToken(username))
                .refreshToken(generateRefreshToken(username))
                .build();
    }

    @Transactional
    public void deleteRefreshToken(String token) {
        refreshTokenRepository.deleteByToken(token);
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiredAt().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(token); // Xóa khỏi DB vì đã vô dụng
            throw new TokenExpiredException("Refresh token đã hết hạn. Vui lòng đăng nhập lại.");
        }
        return token;
    }

    public UserResponse getUserResponseFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        Long userId = Long.parseLong(claims.getSubject());
        String role = claims.get("role").toString();
        boolean expired = claims.getExpiration().before(new Date());
        return new UserResponse(userId, role, expired);
    }
}
