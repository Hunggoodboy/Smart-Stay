package vn.edu.ptit.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "admins")
public class Admins implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "role", nullable = false)
    private String role = "MODERATOR";

    @Column(name = "is_active", nullable = false)
    private Boolean active = Boolean.FALSE;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Admins không có quan hệ JPA trực tiếp với các entity khác.
    // Notifications gửi đến Admin được xử lý qua recipientType = "ADMIN"
    // và recipientId tham chiếu đến admins.id (polymorphic, không dùng FK cứng).
}
