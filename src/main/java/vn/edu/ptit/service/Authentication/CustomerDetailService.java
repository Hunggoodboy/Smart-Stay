package vn.edu.ptit.service.Authentication;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import vn.edu.ptit.entity.User;
import vn.edu.ptit.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class CustomerDetailService implements UserDetailsService {
    private final UserRepository userRepository;
        @Override
    public UserDetails loadUserByUsername(String username) {
            System.out.println(username);
        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("Can't find"));
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .build();
    }

}
