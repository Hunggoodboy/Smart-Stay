package vn.edu.ptit.dto.Response;


import lombok.Data;
import vn.edu.ptit.dto.UserDTO;

@Data
public class AuthResponse {
    private String message;
    private boolean success;
    private UserDTO user;

    public AuthResponse(String message, boolean success, UserDTO user) {
        this.message = message;
        this.success = success;
        this.user = user;
    }
}
