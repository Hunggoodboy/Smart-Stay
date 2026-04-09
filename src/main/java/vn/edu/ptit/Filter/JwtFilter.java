package vn.edu.ptit.Filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import vn.edu.ptit.dto.Response.UserResponse;
import vn.edu.ptit.service.JWT.jwtService;

import java.io.IOException;
import java.util.Collections;


@Component
@AllArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final jwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        try {
            // Cắt bear
            String token = authHeader.substring(7);
            UserResponse userResponse = jwtService.getUserResponseFromToken(token);
            if (userResponse != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                SimpleGrantedAuthority authority = new SimpleGrantedAuthority(userResponse.getRole());
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userResponse.getId(),
                        null,
                        Collections.singletonList(authority) // roles
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        catch (Exception e) {
                System.out.println("JWT Invalid: " + e.getMessage());
            }
        filterChain.doFilter(request, response);
    }

}
