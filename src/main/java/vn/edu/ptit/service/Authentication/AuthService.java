package vn.edu.ptit.service.Authentication;

import jakarta.servlet.http.Cookie;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import vn.edu.ptit.dto.Request.ChangePasswordRequest;
import vn.edu.ptit.dto.Request.UpdateProfileRequest;
import vn.edu.ptit.dto.Request.UpgradeCustomerRequest;
import vn.edu.ptit.dto.Response.ApiResponse;
import vn.edu.ptit.dto.Response.AuthResponse;
import vn.edu.ptit.dto.Request.LoginRequest;
import vn.edu.ptit.dto.Request.RegisterRequest;
import vn.edu.ptit.dto.Response.UserResponse;
import vn.edu.ptit.dto.UserDTO;
import vn.edu.ptit.entity.LandLord;
import vn.edu.ptit.entity.User;
import vn.edu.ptit.entity.Customer;
import vn.edu.ptit.repository.LandLordRepository;
import vn.edu.ptit.repository.UserRepository;
import vn.edu.ptit.service.JWT.jwtService;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor

public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();
    private final LandLordRepository landLordRepository;
    private final AuthenticationManager authManager;
    private final jwtService jwtService;

    private boolean checkUsernameExists(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    public AuthResponse register(RegisterRequest registerRequest) {
        try {
            if (registerRequest.getUsername() == null || registerRequest.getPassword() == null
                    || registerRequest.getConfirmPassword() == null || registerRequest.getFullName() == null
                    || registerRequest.getPhoneNumber() == null) {
                return new AuthResponse("Vui lòng điền đầy đủ thông tin", false, null, null);
            } else if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
                return new AuthResponse("Mật khẩu và xác nhận mật khẩu không khớp", false, null, null);
            } else if (checkUsernameExists(registerRequest.getUsername())) {
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
        } catch (Exception e) {
            return new AuthResponse("Đăng ký thất bại: " + e.getMessage(), false, null, null);
        }
    }

    public AuthResponse login(LoginRequest loginRequest, HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {
        try {
            String username = loginRequest.getUsername();
            String password = loginRequest.getPassword();
            Authentication authentication = authManager
                    .authenticate(new UsernamePasswordAuthenticationToken(username, password));
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
            securityContextRepository.saveContext(context, httpRequest, httpResponse);
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Tên đăng nhập hoặc mật khẩu không chính xác"));
            String token = jwtService.generateToken(user.getUsername());
            Cookie tokenCookie = new Cookie("smartstay_token", token);
            tokenCookie.setHttpOnly(true);
            tokenCookie.setPath("/");
            tokenCookie.setMaxAge(24 * 60 * 60);
            httpResponse.addCookie(tokenCookie);
            return new AuthResponse("Đăng nhập thành công", true, UserDTO.fromEntity(user), token);
        } catch (BadCredentialsException e) {
            return new AuthResponse("Tên đăng nhập hoặc mật khẩu không chính xác", false, null, null);
        } catch (Exception ex) {
            return new AuthResponse("Đăng nhập thất bại: " + ex.getMessage(), false, null, null);
        }
    }

    public ApiResponse upgradeCustomer(UpgradeCustomerRequest request) {
        Long currentId = getCurrentUserId();
        userRepository.updateUserType("TENANT", currentId);
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

    @Transactional
    public UserDTO updateCurrentUser(UpdateProfileRequest request) {
        Long userId = getCurrentUserId();
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        if (StringUtils.hasText(request.getFullName())) {
            user.setFullName(request.getFullName().trim());
        }
        if (StringUtils.hasText(request.getEmail())) {
            user.setEmail(request.getEmail().trim());
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber().trim());
        }
        if (request.getGender() != null) {
            user.setGender(request.getGender().trim());
        }
        user.setDateOfBirth(request.getDateOfBirth());
        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl().trim());
        }
        if (user instanceof Customer customer) {
            if (request.getIdCardNumber() != null) {
                customer.setIdCardNumber(request.getIdCardNumber().trim());
            }
            if (request.getAddress() != null) {
                customer.setAddress(request.getAddress().trim());
            }
        } else if (user instanceof LandLord landLord) {
            if (request.getIdCardNumber() != null) {
                landLord.setIdCardNumber(request.getIdCardNumber().trim());
            }
            if (request.getAddress() != null) {
                landLord.setAddress(request.getAddress().trim());
            }
        }
        user.setUpdatedAt(LocalDateTime.now());

        return UserDTO.fromEntity(userRepository.save(user));
    }

    @Transactional
    public ApiResponse changePassword(ChangePasswordRequest request) {
        if (!StringUtils.hasText(request.getCurrentPassword())
                || !StringUtils.hasText(request.getNewPassword())
                || !StringUtils.hasText(request.getConfirmPassword())) {
            return ApiResponse.builder()
                    .success(false)
                    .message("Vui lòng nhập đầy đủ thông tin")
                    .build();
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            return ApiResponse.builder()
                    .success(false)
                    .message("Mật khẩu mới và xác nhận mật khẩu không khớp")
                    .build();
        }

        if (request.getNewPassword().length() < 6) {
            return ApiResponse.builder()
                    .success(false)
                    .message("Mật khẩu mới phải có ít nhất 6 ký tự")
                    .build();
        }

        Long userId = getCurrentUserId();
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            return ApiResponse.builder()
                    .success(false)
                    .message("Mật khẩu hiện tại không chính xác")
                    .build();
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        return ApiResponse.builder()
                .success(true)
                .message("Đổi mật khẩu thành công")
                .build();
    }

    public User getUser() {
        Long userId = getCurrentUserId();
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        return user;
    }

    public Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new RuntimeException("Authentication object is null");
        }
        if (authentication instanceof org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken) {
            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
            return userRepository.findByEmail(oauth2User.getAttribute("email")).orElseThrow(
                    () -> new RuntimeException("User not found with email: " + oauth2User.getAttribute("email")))
                    .getId();
        }
        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getPrincipal().equals("anonymousUser")) {
            throw new IllegalStateException("Người dùng chưa đăng nhập");
        }
        return Long.parseLong(authentication.getName());
    }

    public LandLord getCurrentLandLord() {
        Long currentUserId = getCurrentUserId();
        LandLord landLord = landLordRepository.findLandLordById(currentUserId).orElseThrow(() -> new RuntimeException(
                "Bạn chưa đăng ký diện chủ nhà, vui lòng gửi yêu cầu đăng ký bạn là chủ nhà trước"));
        return landLord;
    }

    public Long getCurrentUserIdOrNull() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Nếu không có ai, hoặc là anonymousUser (Khách vãng lai) -> Trả về null
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
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
