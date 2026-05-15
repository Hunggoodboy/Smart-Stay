package vn.edu.ptit.service.Authentication;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;
import vn.edu.ptit.dto.Request.UpgradeCustomerRequest;
import vn.edu.ptit.dto.Response.ApiResponse;
import vn.edu.ptit.dto.Response.AuthResponse;
import vn.edu.ptit.dto.Request.LoginRequest;
import vn.edu.ptit.dto.Request.RegisterRequest;
import vn.edu.ptit.dto.Response.UserResponse;
import vn.edu.ptit.dto.UserDTO;
import vn.edu.ptit.entity.LandLord;
import vn.edu.ptit.entity.User;
import vn.edu.ptit.repository.LandLordRepository;
import vn.edu.ptit.repository.UserRepository;
import vn.edu.ptit.service.JWT.jwtService;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor

public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityContextRepository securityContextRepository =
            new HttpSessionSecurityContextRepository();
    private final LandLordRepository landLordRepository;
    private final AuthenticationManager authManager;
    private final jwtService jwtService;

    private boolean checkUsernameExists(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    public AuthResponse register(RegisterRequest registerRequest) {
        try {
            if(registerRequest.getUsername() == null || registerRequest.getPassword() == null || registerRequest.getConfirmPassword() == null || registerRequest.getFullName() == null || registerRequest.getPhoneNumber() == null) {
                return new AuthResponse("Vui lòng điền đầy đủ thông tin", false, null, null);
            }
            else if(!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
                return new AuthResponse("Mật khẩu và xác nhận mật khẩu không khớp", false, null, null);
            }
            else if(checkUsernameExists(registerRequest.getUsername())) {
                return new AuthResponse("Tên đăng nhập đã tồn tại", false, null, null);
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
            return new AuthResponse("Đăng ký thành công", true, userDTO, null);
        }
        catch (Exception e) {
            return new AuthResponse("Đăng ký thất bại: " + e.getMessage(), false, null, null);
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
            String token = jwtService.generateToken(user.getUsername());
            return new AuthResponse("Đăng nhập thành công", true, UserDTO.fromEntity(user),  token);
        }
        catch (BadCredentialsException e) {
            return new AuthResponse("Tên đăng nhập hoặc mật khẩu không chính xác", false, null, null);
        }
        catch (Exception ex) {
            return new AuthResponse("Đăng nhập thất bại: " + ex.getMessage(), false, null, null);
        }
    }
    public ApiResponse upgradeCustomer(UpgradeCustomerRequest request){
        Long currentId = getCurrentUserId();
        userRepository.updateUserType(currentId);
        userRepository.insertIntoCustomer(currentId, request.getAddress(), request.getIdCardNumber());
        return ApiResponse.builder()
                .success(true)
                .message("Bạn đã đăng ký thành diện khách hàng thành công")
                .build();
    }

    public UserDTO getCurrentUser() {
        Long userId = getCurrentUserId();
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        return UserDTO.fromEntity(user);
    }
    public User getUser() {
        Long userId = getCurrentUserId();
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        return user;
    }
    public Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication == null) {
            throw new RuntimeException("Authentication object is null");
        }
        if(authentication instanceof org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken) {
            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
            return userRepository.findByEmail(oauth2User.getAttribute("email")).orElseThrow(() -> new RuntimeException("User not found with email: " + oauth2User.getAttribute("email"))).getId();
        }
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            throw new IllegalStateException("Người dùng chưa đăng nhập");
        }
        return Long.parseLong(authentication.getName());
    }

    public LandLord getCurrentLandLord() {
        Long currentUserId = getCurrentUserId();
        LandLord landLord = landLordRepository.findLandLordById(currentUserId).orElseThrow(() -> new RuntimeException("Bạn chưa đăng ký diện chủ nhà, vui lòng gửi yêu cầu đăng ký bạn là chủ nhà trước"));
        return landLord;
    }

    public Long getCurrentUserIdOrNull() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Nếu không có ai, hoặc là anonymousUser (Khách vãng lai) -> Trả về null
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }

        try {
            // Nếu đã giải mã thành công ở JwtFilter, tên đăng nhập sẽ nằm ở đây
            return Long.parseLong(authentication.getName());
        } catch (Exception e) {
            return null;
        }
    }
}
