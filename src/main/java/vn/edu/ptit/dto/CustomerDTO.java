package vn.edu.ptit.dto;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

public class CustomerDTO {
    private Long id;
    private String fullName;
    private String email;
    private String phoneNumber;

}
