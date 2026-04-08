package vn.edu.ptit.service.JWT;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import vn.edu.ptit.dto.Response.UserResponse;
import vn.edu.ptit.repository.UserRepository;
import vn.edu.ptit.service.Authentication.CustomerDetailService;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
@AllArgsConstructor
public class jwtService {
    private final String jwtSecret = "84985f37-a3aa-4137-aa09-44991a9aecf6";
    private final long jwtExpirationMs = 86400000;
    private final CustomerDetailService customerDetailService;
    private final UserRepository userRepository;
    private SecretKey getSigningKey() {
        byte[] keyBytes = this.jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
    public String generateToken(String username) {
        UserDetails userDetails = customerDetailService.loadUserByUsername(username);
        Long userId = userRepository.findByUsername(username).get().getId();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);
        return Jwts.builder()
                .subject(userId.toString())
                .claim("role", userDetails.getAuthorities().iterator().next().getAuthority().substring(5))
                .issuedAt(now)
                .signWith(getSigningKey())
                .expiration(expiryDate)
                .compact();
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
