package vn.edu.ptit.service.Authentication;

import io.jsonwebtoken.Jwts;
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
import org.springframework.web.multipart.MultipartFile;
import vn.edu.ptit.dto.Request.ChangePasswordRequest;
import vn.edu.ptit.dto.Request.UpdateProfileRequest;
import vn.edu.ptit.dto.Request.UpgradeCustomerRequest;
import vn.edu.ptit.dto.Response.ApiResponse;
import vn.edu.ptit.dto.Response.AuthResponse;
import vn.edu.ptit.dto.Request.LoginRequest;
import vn.edu.ptit.dto.Request.RegisterRequest;
import vn.edu.ptit.dto.Response.TokenResponse;
import vn.edu.ptit.dto.Response.UserResponse;
import vn.edu.ptit.dto.UserDTO;
import vn.edu.ptit.entity.LandLord;
import vn.edu.ptit.entity.RefreshToken;
import vn.edu.ptit.entity.User;
import vn.edu.ptit.entity.Customer;
import vn.edu.ptit.repository.LandLordRepository;
import vn.edu.ptit.repository.RefreshTokenRepository;
import vn.edu.ptit.repository.UserRepository;
import vn.edu.ptit.service.JWT.jwtService;
import vn.edu.ptit.Exception.ResourceNotFoundException;
import vn.edu.ptit.Exception.UnauthorizedException;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor

public class AuthService {
    private static final long MAX_AVATAR_BYTES = 5L * 1024 * 1024;
    private static final Path SOURCE_AVATAR_DIR = Paths.get("src/main/resources/static/images/avatars");
    private static final Path TARGET_AVATAR_DIR = Paths.get("target/classes/static/images/avatars");

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();
    private final LandLordRepository landLordRepository;
    private final AuthenticationManager authManager;
    private final jwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;

    private boolean checkUsernameExists(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    public AuthResponse register(RegisterRequest registerRequest) {
        try {
            if (registerRequest.getUsername() == null || registerRequest.getPassword() == null
                    || registerRequest.getConfirmPassword() == null || registerRequest.getFullName() == null
                    || registerRequest.getPhoneNumber() == null) {
                return new AuthResponse("Vui lòng điền đầy đủ thông tin", false, null, null, null);
            } else if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
                return new AuthResponse("Mật khẩu và xác nhận mật khẩu không khớp", false, null, null, null);
            } else if (checkUsernameExists(registerRequest.getUsername())) {
                return new AuthResponse("Tên đăng nhập đã tồn tại", false, null, null, null);
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
            return new AuthResponse("Đăng ký thành công", true, userDTO, null, null);
        } catch (Exception e) {
            return new AuthResponse("Đăng ký thất bại: " + e.getMessage(), false, null, null, null);
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
            TokenResponse token = jwtService.generateToken(user.getUsername());
            Cookie tokenCookie = new Cookie("smartstay_token", token.getAccessToken());
            Cookie refreshTokenCookie = new Cookie("refresh_token", token.getRefreshToken());
            tokenCookie.setHttpOnly(true);
            tokenCookie.setPath("/");
            tokenCookie.setMaxAge(24 * 60 * 60);
            httpResponse.addCookie(tokenCookie);
            httpResponse.addCookie(refreshTokenCookie);
            return new AuthResponse("Đăng nhập thành công", true, UserDTO.fromEntity(user), token.getAccessToken(), token.getRefreshToken());
        } catch (BadCredentialsException e) {
            return new AuthResponse("Tên đăng nhập hoặc mật khẩu không chính xác", false, null, null, null);
        } catch (Exception ex) {
            return new AuthResponse("Đăng nhập thất bại: " + ex.getMessage(), false, null, null, null);
        }
    }

    public ApiResponse logout(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        try{
            String refreshToken = null;
            Cookie[] cookies = httpRequest.getCookies();
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("smartstay_token")) {
                    refreshToken = cookie.getValue();
                }
            }
            if (refreshToken != null) {
                jwtService.deleteRefreshToken(refreshToken);
            }
            Cookie tokenCookie = new Cookie("smartstay_token", null);
            tokenCookie.setHttpOnly(true);
            tokenCookie.setPath("/");
            tokenCookie.setMaxAge(0);
            httpResponse.addCookie(tokenCookie);
            Cookie refreshTokenCookie = new Cookie("refresh_token", null);
            tokenCookie.setHttpOnly(true);
            tokenCookie.setPath("/");
            tokenCookie.setMaxAge(0);
            httpResponse.addCookie(tokenCookie);
            httpResponse.addCookie(refreshTokenCookie);
            SecurityContextHolder.clearContext();
            return ApiResponse.builder()
                    .success(true)
                    .message("Đăng xuất thành công")
                    .build();
        }
        catch (Exception e){
            throw new RuntimeException("Lỗi khi đăng xuất: " + e.getMessage(), e);
        }
    }

    public AuthResponse generateTokenByRefreshToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refresh_token".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }

        if (refreshToken == null) {
            return new AuthResponse("Không tìm thấy Refresh Token", false, null, null, null);
        }

        try {
            RefreshToken refreshTokenObj = refreshTokenRepository.findByToken(refreshToken)
                    .orElseThrow(() -> new RuntimeException("Refresh Token không hợp lệ hoặc không tồn tại"));
            jwtService.verifyExpiration(refreshTokenObj);
            User user = refreshTokenObj.getUser();
            TokenResponse token = jwtService.generateToken(user.getUsername());
            Cookie tokenCookie = new Cookie("smartstay_token", token.getAccessToken());
            tokenCookie.setHttpOnly(true);
            tokenCookie.setPath("/");
            tokenCookie.setMaxAge(24 * 60 * 60); // 1 ngày
            response.addCookie(tokenCookie);

            Cookie newRefreshTokenCookie = new Cookie("refresh_token", token.getRefreshToken());
            newRefreshTokenCookie.setHttpOnly(true);
            newRefreshTokenCookie.setPath("/");
            newRefreshTokenCookie.setMaxAge(14 * 24 * 60 * 60); // 14 ngày
            response.addCookie(newRefreshTokenCookie);

            jwtService.deleteRefreshToken(refreshToken);
            return new AuthResponse("Làm mới token thành công", true, UserDTO.fromEntity(user), token.getAccessToken(), token.getRefreshToken());
        }
        catch (Exception e){
            return new AuthResponse("Lỗi refresh token: " + e.getMessage(), false, null, null, null);
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
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        return UserDTO.fromEntity(user);
    }

    @Transactional
    public UserDTO updateCurrentUser(UpdateProfileRequest request) {
        return updateCurrentUser(request, null);
    }

    @Transactional
    public UserDTO updateCurrentUser(UpdateProfileRequest request, MultipartFile avatarFile) {
        Long userId = getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

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
        if (avatarFile != null && !avatarFile.isEmpty()) {
            user.setAvatarUrl(saveUploadedAvatarToStatic(avatarFile));
        } else if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(resolveAvatarUrl(request.getAvatarUrl()));
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

    private String saveUploadedAvatarToStatic(MultipartFile avatarFile) {
        String contentType = avatarFile.getContentType();
        if (StringUtils.hasText(contentType)
                && !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw new IllegalArgumentException("File ảnh đại diện phải là ảnh hợp lệ");
        }
        if (avatarFile.getSize() > MAX_AVATAR_BYTES) {
            throw new IllegalArgumentException("Ảnh đại diện không được vượt quá 5MB");
        }
        if (avatarFile.isEmpty()) {
            throw new IllegalArgumentException("Ảnh đại diện tải lên bị rỗng");
        }

        String extension = avatarExtension(contentType, avatarFile.getOriginalFilename());
        if (!StringUtils.hasText(extension)) {
            throw new IllegalArgumentException("Ảnh đại diện chỉ hỗ trợ JPG, PNG, WEBP hoặc GIF");
        }

        try {
            byte[] imageBytes = avatarFile.getBytes();
            String filename = UUID.randomUUID() + extension;
            writeAvatarFile(SOURCE_AVATAR_DIR, filename, imageBytes);
            if (Files.exists(Paths.get("target/classes/static"))) {
                writeAvatarFile(TARGET_AVATAR_DIR, filename, imageBytes);
            }
            return "/images/avatars/" + filename;
        } catch (IOException e) {
            throw new IllegalArgumentException("Không thể lưu ảnh đại diện vào static", e);
        }
    }

    private String resolveAvatarUrl(String avatarUrl) {
        String value = avatarUrl == null ? "" : avatarUrl.trim();
        if (!StringUtils.hasText(value)) {
            return null;
        }
        if (value.startsWith("/images/") || value.startsWith("images/")) {
            return value.startsWith("/") ? value : "/" + value;
        }
        if (!isHttpUrl(value)) {
            throw new IllegalArgumentException("URL ảnh đại diện phải bắt đầu bằng http:// hoặc https://");
        }
        return downloadAvatarToStatic(value);
    }

    private boolean isHttpUrl(String value) {
        try {
            URI uri = new URI(value);
            String scheme = uri.getScheme();
            return ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme))
                    && StringUtils.hasText(uri.getHost());
        } catch (URISyntaxException e) {
            return false;
        }
    }

    private String downloadAvatarToStatic(String avatarUrl) {
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .build();
            HttpRequest request = HttpRequest.newBuilder(URI.create(avatarUrl))
                    .timeout(Duration.ofSeconds(20))
                    .GET()
                    .build();
            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalArgumentException("Không tải được ảnh đại diện từ URL đã nhập");
            }

            String contentType = response.headers().firstValue("content-type").orElse("");
            if (StringUtils.hasText(contentType)
                    && !contentType.toLowerCase(Locale.ROOT).startsWith("image/")
                    && !contentType.toLowerCase(Locale.ROOT).startsWith("application/octet-stream")) {
                throw new IllegalArgumentException("URL ảnh đại diện phải trỏ tới file ảnh hợp lệ");
            }
            String extension = avatarExtension(contentType, avatarUrl);
            if (!StringUtils.hasText(extension)) {
                throw new IllegalArgumentException("URL ảnh đại diện phải trỏ tới file ảnh hợp lệ");
            }

            byte[] imageBytes = response.body();
            if (imageBytes.length == 0) {
                throw new IllegalArgumentException("Ảnh đại diện tải về bị rỗng");
            }
            if (imageBytes.length > MAX_AVATAR_BYTES) {
                throw new IllegalArgumentException("Ảnh đại diện không được vượt quá 5MB");
            }

            String filename = UUID.randomUUID() + extension;
            writeAvatarFile(SOURCE_AVATAR_DIR, filename, imageBytes);
            if (Files.exists(Paths.get("target/classes/static"))) {
                writeAvatarFile(TARGET_AVATAR_DIR, filename, imageBytes);
            }
            return "/images/avatars/" + filename;
        } catch (IOException e) {
            throw new IllegalArgumentException("Không thể lưu ảnh đại diện vào static", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalArgumentException("Quá trình tải ảnh đại diện bị gián đoạn", e);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("URL ảnh đại diện không hợp lệ", e);
        }
    }

    private void writeAvatarFile(Path directory, String filename, byte[] imageBytes) throws IOException {
        Files.createDirectories(directory);
        Files.write(directory.resolve(filename), imageBytes);
    }

    private String avatarExtension(String contentType, String avatarUrl) {
        String normalizedContentType = contentType == null ? "" : contentType.toLowerCase(Locale.ROOT);
        if (normalizedContentType.contains(";")) {
            normalizedContentType = normalizedContentType.substring(0, normalizedContentType.indexOf(";")).trim();
        }
        return switch (normalizedContentType) {
            case "image/jpeg", "image/jpg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            case "image/gif" -> ".gif";
            default -> extensionFromPath(avatarUrl);
        };
    }

    private String extensionFromUrl(String avatarUrl) {
        String path = URI.create(avatarUrl).getPath();
        return extensionFromPath(path);
    }

    private String extensionFromPath(String path) {
        if (!StringUtils.hasText(path) || !path.contains(".")) {
            return null;
        }
        String extension = path.substring(path.lastIndexOf(".")).toLowerCase(Locale.ROOT);
        return switch (extension) {
            case ".jpg", ".jpeg", ".png", ".webp", ".gif" -> extension;
            default -> null;
        };
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
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
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
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        return user;
    }

    public Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new UnauthorizedException("Không thể xác thực người dùng, vui lòng đăng nhập lại");
        }
        if (authentication instanceof org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken) {
            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
            return userRepository.findByEmail(oauth2User.getAttribute("email"))
                    .orElseThrow(() -> new ResourceNotFoundException("User", "email", oauth2User.getAttribute("email")))
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
        LandLord landLord = landLordRepository.findLandLordById(currentUserId)
                .orElseThrow(() -> new UnauthorizedException(
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

    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        return UserDTO.fromEntity(user);
    }
}
