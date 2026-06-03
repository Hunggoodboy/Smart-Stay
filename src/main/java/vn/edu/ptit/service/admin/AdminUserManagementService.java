package vn.edu.ptit.service.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.ptit.dto.Response.AdminUserResponse;
import vn.edu.ptit.dto.Response.ApiResponse;
import vn.edu.ptit.entity.User;
import vn.edu.ptit.repository.AdminUserManagementRepository;
import vn.edu.ptit.service.Authentication.AuthService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminUserManagementService {

    private final AdminUserManagementRepository adminUserRepository;
    private final AuthService authService;

    @Transactional(readOnly = true)
    public List<AdminUserResponse> getAllUsers(boolean includeDeleted) {
        return adminUserRepository.findAllForAdmin(includeDeleted)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AdminUserResponse> getUsersByRole(User.Role role, boolean includeDeleted) {
        return adminUserRepository.findByRoleForAdmin(role, includeDeleted)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AdminUserResponse> getUsersByStatus(Boolean active, boolean includeDeleted) {
        if (active == null) {
            throw new RuntimeException("Trang thai tai khoan khong duoc de trong");
        }

        return adminUserRepository.findByStatusForAdmin(active, includeDeleted)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AdminUserResponse> getUsersByKeyword(String keyword, boolean includeDeleted) {
        String normalizedKeyword = normalizeKeyword(keyword);
        if (normalizedKeyword.isEmpty()) {
            return getAllUsers(includeDeleted);
        }

        return adminUserRepository.findByKeywordForAdmin(normalizedKeyword, includeDeleted)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AdminUserResponse getUser(Long id) {
        return toResponse(findUser(id));
    }

    @Transactional(readOnly = true)
    public long countLockedUsers() {
        return adminUserRepository.countLockedUsers();
    }

    @Transactional
    public ApiResponse updateStatus(Long id, Boolean active) {
        if (active == null) {
            throw new RuntimeException("Trang thai tai khoan khong duoc de trong");
        }

        User user = findUser(id);
        preventSelfManagement(id, "Khong the khoa hoac mo khoa chinh tai khoan dang dang nhap");

        user.setActive(active);
        user.setUpdatedAt(LocalDateTime.now());
        adminUserRepository.save(user);

        return ApiResponse.builder()
                .success(true)
                .message(active ? "Mo khoa tai khoan thanh cong" : "Khoa tai khoan thanh cong")
                .build();
    }

    @Transactional
    public ApiResponse updateRole(Long id, User.Role role) {
        if (role == null) {
            throw new RuntimeException("Vai tro tai khoan khong duoc de trong");
        }

        User user = findUser(id);
        preventSelfManagement(id, "Khong the doi quyen chinh tai khoan dang dang nhap");

        user.setRole(role);
        user.setUpdatedAt(LocalDateTime.now());
        adminUserRepository.save(user);

        return ApiResponse.builder()
                .success(true)
                .message("Cap nhat vai tro tai khoan thanh cong")
                .build();
    }

    @Transactional
    public ApiResponse softDelete(Long id) {
        User user = findUser(id);
        preventSelfManagement(id, "Khong the xoa chinh tai khoan dang dang nhap");

        user.setActive(false);
        user.setDeletedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        adminUserRepository.save(user);

        return ApiResponse.builder()
                .success(true)
                .message("Xoa tai khoan thanh cong")
                .build();
    }

    @Transactional
    public ApiResponse restore(Long id) {
        User user = findUser(id);

        user.setDeletedAt(null);
        user.setActive(true);
        user.setUpdatedAt(LocalDateTime.now());
        adminUserRepository.save(user);

        return ApiResponse.builder()
                .success(true)
                .message("Khoi phuc tai khoan thanh cong")
                .build();
    }

    private User findUser(Long id) {
        return adminUserRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Khong tim thay tai khoan"));
    }

    private void preventSelfManagement(Long userId, String message) {
        Long currentUserId = authService.getCurrentUserId();
        if (currentUserId != null && currentUserId.equals(userId)) {
            throw new RuntimeException(message);
        }
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return "";
        }
        return keyword.trim();
    }

    private AdminUserResponse toResponse(User user) {
        return AdminUserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole())
                .active(user.getActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .deletedAt(user.getDeletedAt())
                .build();
    }
}
