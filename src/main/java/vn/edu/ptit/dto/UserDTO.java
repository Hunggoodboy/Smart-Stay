package vn.edu.ptit.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import vn.edu.ptit.entity.User;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String gender;
    private LocalDate dateOfBirth;
    private String avatarUrl;
    public static UserDTO fromEntity(User user) {
        return new UserDTO(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getGender(),
                user.getDateOfBirth(),
                user.getAvatarUrl()
        );
    }
}
