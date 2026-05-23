package vn.edu.ptit.dto.Request;

import lombok.Data;
import vn.edu.ptit.entity.User;

@Data
public class AdminUserRoleRequest {
    private User.Role role;
}
