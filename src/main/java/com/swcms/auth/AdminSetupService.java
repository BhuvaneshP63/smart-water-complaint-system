
package com.swcms.auth;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.swcms.model.User;
import com.swcms.repository.UserRepository;

@Service
public class AdminSetupService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminSetupService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public String createAdmin(
            String name,
            String email,
            String password) {

        if (userRepository.existsByEmail(email)) {
            return "Admin account already exists";
        }

        User admin = new User();

        admin.setName(name);
        admin.setEmail(email);
        admin.setPassword(passwordEncoder.encode(password));
        admin.setRole("ADMIN");

        userRepository.save(admin);

        return "Admin account created successfully";
    }
}
