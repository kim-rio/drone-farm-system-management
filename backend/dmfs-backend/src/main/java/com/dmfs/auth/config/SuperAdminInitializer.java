package com.dmfs.auth.config;

import com.dmfs.auth.entity.Role;
import com.dmfs.auth.entity.User;
import com.dmfs.auth.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class SuperAdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public SuperAdminInitializer(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {

        String email = "superadmin@jmsolutions.com";

        if (userRepository.existsByEmail(email)) {
            return;
        }

        User user = new User();

        user.setEmail(email);
        user.setPassword(passwordEncoder.encode("ChangeMe123!"));
        user.setFirstName("Super");
        user.setLastName("Admin");
        user.setRole(Role.SUPER_ADMIN);
        user.setActive(true);

        userRepository.save(user);

        System.out.println("==============================================");
        System.out.println("SUPER ADMIN ACCOUNT CREATED");
        System.out.println("Email: " + email);
        System.out.println("Password: ChangeMe123!");
        System.out.println("==============================================");
    }
}
