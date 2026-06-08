package vn.edu.ptit.dto.Response;


import lombok.Data;
import vn.edu.ptit.dto.UserDTO;

@Data
public class AuthResponse {
    private String message;
    private boolean success;
    private String accessToken;
    private String refreshToken;
    private UserDTO user;

    public AuthResponse(String message, boolean success, UserDTO user, String accessToken, String refreshToken) {
        this.message = message;
        this.success = success;
        this.user = user;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
}
