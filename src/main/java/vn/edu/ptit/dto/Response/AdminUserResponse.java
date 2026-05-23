package vn.edu.ptit.dto.Response;

import lombok.Builder;
import lombok.Data;
import vn.edu.ptit.entity.User;

import java.time.LocalDateTime;

@Data
@Builder
public class AdminUserResponse {
    private Long id;
    private String username;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String avatarUrl;
    private User.Role role;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
}
