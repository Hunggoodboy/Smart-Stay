package vn.edu.ptit.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Table(name = "admins")
@DiscriminatorValue("ADMIN")
@PrimaryKeyJoinColumn(name = "user_id")
public class Admin extends User {

    private static final long serialVersionUID = 1L;

    @Column(name = "is_super_admin", nullable = false)
    private Boolean superAdmin = false;

}