package vn.edu.ptit.dto;

import lombok.AllArgsConstructor;

import java.time.LocalDate;

@AllArgsConstructor
public class UserRegisterDTO {
    private String fullName;
    private String password;
    private String confirmPassword;
    private String email;
    private String phoneNumber;
    private String gender;
    private LocalDate dateOfBirth;
}
