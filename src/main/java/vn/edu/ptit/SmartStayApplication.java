package vn.edu.ptit;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import vn.edu.ptit.entity.User;
import vn.edu.ptit.repository.UserRepository;

import java.time.LocalDateTime;

@SpringBootApplication
public class SmartStayApplication {

	public static void main(String[] args) {
		SpringApplication.run(SmartStayApplication.class, args);
	}

	@Bean
	CommandLineRunner seedTestUser(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		return args -> {
			final String username = "testuser";
			final String password = "Test@123";

			User user = userRepository.findByUsername(username).orElseGet(User::new);
			user.setUsername(username);
			user.setPassword(passwordEncoder.encode(password));
			user.setFullName("Test User");
			user.setPhoneNumber("0900000000");
			user.setRole(User.Role.TENANT);
			user.setActive(true);
			user.setEmail("testuser@smartstay.local");
			if (user.getCreatedAt() == null) {
				user.setCreatedAt(LocalDateTime.now());
			}

			userRepository.save(user);
		};
	}

}
