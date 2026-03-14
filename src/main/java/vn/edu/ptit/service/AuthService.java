package vn.edu.ptit.service;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;
import vn.edu.ptit.dto.AuthResponse;
import vn.edu.ptit.dto.LoginRequest;
import vn.edu.ptit.dto.RegisterRequest;
import vn.edu.ptit.dto.UserDTO;
import vn.edu.ptit.entity.User;
import vn.edu.ptit.repository.UserRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authManager;
    private final SecurityContextRepository securityContextRepository =
            new HttpSessionSecurityContextRepository();
    private boolean checkUsernameExists(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    public AuthResponse register(RegisterRequest registerRequest) {
        try {
            if(registerRequest.getUsername() == null || registerRequest.getPassword() == null || registerRequest.getConfirmPassword() == null || registerRequest.getFullName() == null || registerRequest.getPhoneNumber() == null) {
                return new AuthResponse("Vui lòng điền đầy đủ thông tin", false, null);
            }
            else if(!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
                return new AuthResponse("Mật khẩu và xác nhận mật khẩu không khớp", false, null);
            }
            else if(checkUsernameExists(registerRequest.getUsername())) {
                return new AuthResponse("Tên đăng nhập đã tồn tại", false, null);
            }
            User user = new User();
            user.setUsername(registerRequest.getUsername());
            user.setFullName(registerRequest.getFullName());
            user.setEmail(registerRequest.getEmail());
            user.setPhoneNumber(registerRequest.getPhoneNumber());
            user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
            user.setRole(User.Role.CUSTOMER);
            user.setActive(true);
            user.setCreatedAt(LocalDateTime.now());
            userRepository.save(user);
            UserDTO userDTO = UserDTO.fromEntity(user);
            return new AuthResponse("Đăng ký thành công", true, userDTO);
        }
        catch (Exception e) {
            return new AuthResponse("Đăng ký thất bại: " + e.getMessage(), false, null);
        }
    }
    public AuthResponse login(LoginRequest loginRequest, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        try{
            String username = loginRequest.getUsername();
            String password = loginRequest.getPassword();
            Authentication authentication = authManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
            securityContextRepository.saveContext(context, httpRequest, httpResponse);
            User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("Tên đăng nhập hoặc mật khẩu không chính xác"));
            return new AuthResponse("Đăng nhập thành công", true, UserDTO.fromEntity(user));
        }
        catch (BadCredentialsException e) {
            return new AuthResponse("Tên đăng nhập hoặc mật khẩu không chính xác", false, null);
        }
        catch (Exception ex) {
            return new AuthResponse("Đăng nhập thất bại: " + ex.getMessage(), false, null);
        }
    }
    public void logout(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        SecurityContextHolder.clearContext();
        securityContextRepository.saveContext(SecurityContextHolder.createEmptyContext(), httpRequest, httpResponse);
    }

    public ResponseEntity<?> getTenantDashboardData(Authentication authentication) {
        if(authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow(() -> new RuntimeException("User not found"));
        return new ResponseEntity<>(UserDTO.fromEntity(user), HttpStatus.OK);
    }
}
