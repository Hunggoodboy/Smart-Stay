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

}
