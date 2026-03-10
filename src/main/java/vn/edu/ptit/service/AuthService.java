package vn.edu.ptit.service;

import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vn.edu.ptit.dto.AuthResponse;
import vn.edu.ptit.dto.LoginRequest;
import vn.edu.ptit.dto.RegisterRequest;
import vn.edu.ptit.dto.UserDTO;
import vn.edu.ptit.entity.User;
import vn.edu.ptit.repository.UserRepository;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor

public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private boolean checkUsernameExists(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    public AuthResponse Register(RegisterRequest registerRequest) {
        try {
            if(registerRequest.getUsername() == null || registerRequest.getPassword() == null || registerRequest.getConfirmPassword() == null || registerRequest.getFullName() == null || registerRequest.getPhoneNumber() == null) {
                return new AuthResponse("Vui lòng điền đầy đủ thông tin", false, null);
            }
            else if(!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
                return new AuthResponse("Mật khẩu và xác nhận mật khẩu không khớp", false, null);
            }
            else if(checkUsernameExists(registerRequest.getUsername())) {
                return new AuthResponse("Tên đăng nhập đã tồn tại", false, null);
            }
            User user = new User();
            user.setUsername(registerRequest.getUsername());
            user.setFullName(registerRequest.getFullName());
            user.setEmail(registerRequest.getEmail());
            user.setPhoneNumber(registerRequest.getPhoneNumber());
            user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
            user.setRole(User.Role.TENANT);
            user.setActive(true);
            user.setCreatedAt(LocalDateTime.now());
            userRepository.save(user);
            UserDTO userDTO = UserDTO.fromEntity(user);
            return new AuthResponse("Đăng ký thành công", true, userDTO);
        }
        catch (Exception e) {
            return new AuthResponse("Đăng ký thất bại: " + e.getMessage(), false, null);
        }
    }
    public AuthResponse login(LoginRequest loginRequest) {
        try{
            String username = loginRequest.getUsername();
            String password = loginRequest.getPassword();
            if(!userRepository.findByUsername(username).isPresent()) {
                return new AuthResponse("Tên đăng nhập hoặc mật khẩu của bạn không chính xác", false, null);
            }
            User user = userRepository.findByUsername(username).get();
            if(!passwordEncoder.matches(password, user.getPassword())) {
                return new AuthResponse("Tên đăng nhập hoặc mật khẩu của bạn không chính xác", false, null);
            }
            UserDTO userDTO = UserDTO.fromEntity(user);
            return new AuthResponse("Đăng nhập thành công", true, userDTO);
        }
        catch (Exception e) {
            return new AuthResponse("Đăng nhập thất bại: " + e.getMessage(), false, null);
        }
    }
}
