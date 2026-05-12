package vn.edu.ptit.config;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.DefaultSecurityFilterChain;
import vn.edu.ptit.Filter.JwtFilter;

@EnableWebSecurity
@Configuration
@AllArgsConstructor
public class SecurityConfig {
    private final JwtFilter jwtFilter;

    private static final String[] PUBLIC_URLS = {
            "/", "/login", "/register", "/rooms/**",
            "/css/**", "/js/**", "/images/**",
            "/api/auth/**"
            ,"/room-posted", "/room-detail/**",
            "/api/user/login", "/api/user/register",
            "/postRooms", "/MyRentalRequest", "/myHome", "/payment", "/chatMessage", "/adminVerify", "/registerLandLord", "/error", "/gs-guide-websocket/**"
                ,"/contract/create", "/myContracts", "/contractDetail/**" , "/createRoomManage", "/landlord-view/**" , "/room-detail-management/**"
    };

    private static final String[] LANDLORD_API_URLS = {
            "/api/post-room/**", "/api/billing/**",
            "/api/setBill/**", "/api/landlord/**", "/api/room-management/**"
    };

    private static final String[] AUTHENTICATED_API_URLS = {
            "/api/user/me", "/api/user/myid", "/api/user/tenant",
            "/api/notifications/**", "/api/utility-bills/**",
            "/api/chat/**", "/api/customer/**", "/api/landlord/requestToLandLord"
            ,"/api/contract/**", "/get-my-contracts", "/api/vector/**" , "/api/post-list-room", "/api/chat-ai/**", "/api/delete/request"
    };


    @Bean
    public DefaultSecurityFilterChain SecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .addFilterBefore(jwtFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_URLS).permitAll()
                        .requestMatchers(LANDLORD_API_URLS).hasAuthority("LANDLORD")
                        .requestMatchers("/api/admin/**").hasAuthority("ADMIN")
                        .requestMatchers(AUTHENTICATED_API_URLS).authenticated()
                        .anyRequest().authenticated()
                )
                .csrf(csrf -> csrf.disable());
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    @Bean
    public AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService,
                                                         PasswordEncoder passwordEncoder) throws Exception {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(userDetailsService);
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder);
        return daoAuthenticationProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

}
