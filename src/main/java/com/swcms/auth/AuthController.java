
package com.swcms.auth;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.swcms.model.User;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // User Registration
    @PostMapping("/register")
    public ResponseEntity<String> register(
            @Valid @RequestBody User user) {

        String message = authService.register(user);
        return ResponseEntity.ok(message);
    }
 // Worker Registration
    @PostMapping("/worker/register")
    public ResponseEntity<String> createWorker(
            @Valid @RequestBody User user,
            org.springframework.security.core.Authentication authentication) {

        if (authentication == null ||
            !authentication.getAuthorities().stream()
                .anyMatch(authority ->
                    authority.getAuthority().equals("ROLE_ADMIN"))) {

            return ResponseEntity.status(403)
                    .body("Only Admin can create Worker accounts");
        }

        String message = authService.createWorker(user);

        return ResponseEntity.ok(message);
    }
 // Admin Registration
    @PostMapping("/admin/register")
    public ResponseEntity<String> createAdmin(
            @Valid @RequestBody User user,
            org.springframework.security.core.Authentication authentication) {

        if (authentication == null ||
            !authentication.getAuthorities().stream()
                .anyMatch(authority ->
                    authority.getAuthority().equals("ROLE_ADMIN"))) {

            return ResponseEntity.status(403)
                    .body("Only Admin can create another Admin");
        }

        String message = authService.createAdmin(user);

        return ResponseEntity.ok(message);
    }

    // User Login
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequest request) {
        try {
            String token = authService.login(request.getEmail(), request.getPassword());
            return ResponseEntity.ok(Map.of("token", token));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(401).body(Map.of("message", ex.getMessage()));
        }
    }

    // Login Request DTO
    public static class LoginRequest {

        private String email;
        private String password;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}
