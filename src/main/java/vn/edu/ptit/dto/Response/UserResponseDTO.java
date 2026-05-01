package vn.edu.ptit.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.ptit.entity.User;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDTO {
    private Long id;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String avatarUrl;
    private String role; // Lấy từ Enum Role

    public static UserResponseDTO mapToUserResponse(User user) {
        if (user == null) return null;
        return UserResponseDTO.builder()
                              .id(user.getId())
            .fullName(user.getFullName())
            .email(user.getEmail())
            .phoneNumber(user.getPhoneNumber())
            .avatarUrl(user.getAvatarUrl())
            .role(user.getRole().name())
            .build();
    }
}