package vn.edu.ptit.service;

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
        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("Can't find this user"));
        return org.springframework.security.core.userdetails.User.withUsername(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .build();
    }

}
