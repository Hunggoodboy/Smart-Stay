package vn.edu.ptit.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import vn.edu.ptit.entity.Customer;
import vn.edu.ptit.entity.LandLord;
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
    private String role;
    private String username;
    private String idCardNumber;
    private String address;
    private Boolean verified;

    public static UserDTO fromEntity(User user) {
        String idCardNumber = null;
        String address = null;
        Boolean verified = null;

        if (user instanceof Customer customer) {
            idCardNumber = customer.getIdCardNumber();
            address = customer.getAddress();
            verified = customer.getVerified();
        } else if (user instanceof LandLord landLord) {
            idCardNumber = landLord.getIdCardNumber();
            address = landLord.getAddress();
            verified = landLord.getVerified();
        }

        return new UserDTO(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getGender(),
                user.getDateOfBirth(),
                user.getAvatarUrl(),
                user.getRole() != null ? user.getRole().name() : null,
                user.getUsername(),
                idCardNumber,
                address,
                verified
        );
    }
}
