package com.swcms.controller;

import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import com.swcms.dto.*;
import com.swcms.model.*;
import com.swcms.repository.*;
import com.swcms.security.JwtService;

@RestController
@RequestMapping("/api")
public class ProfileController {
    private final UserRepository users;
    private final ComplaintRepository complaints;
    private final ComplaintStatusHistoryRepository history;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public ProfileController(UserRepository users, ComplaintRepository complaints,
            ComplaintStatusHistoryRepository history, PasswordEncoder encoder, JwtService jwt) {
        this.users = users; this.complaints = complaints; this.history = history; this.encoder = encoder; this.jwt=jwt;
    }

    @GetMapping({"/user/profile", "/worker/profile"})
    public UserProfileResponse getProfile(Authentication auth) { return profile(auth.getName()); }

    @PutMapping({"/user/profile", "/worker/profile"})
    public java.util.Map<String,Object> updateProfile(@Valid @RequestBody ProfileUpdateRequest request, Authentication auth) {
        User user = current(auth);
        if (!user.getEmail().equalsIgnoreCase(request.getEmail()) && users.existsByEmail(request.getEmail()))
            throw new IllegalArgumentException("Email is already registered");
        user.setName(request.getName().trim()); user.setEmail(request.getEmail().trim().toLowerCase());
        User saved=users.save(user);
        return java.util.Map.of("profile", new UserProfileResponse(saved.getUserId(), saved.getName(), saved.getEmail(), saved.getRole()),
                "token", jwt.generateToken(saved.getEmail(), saved.getRole()));
    }

    @PutMapping({"/user/password", "/worker/password"})
    public java.util.Map<String, String> changePassword(@Valid @RequestBody PasswordChangeRequest request, Authentication auth) {
        User user = current(auth);
        if (!encoder.matches(request.getCurrentPassword(), user.getPassword()))
            throw new IllegalArgumentException("Current password is incorrect");
        user.setPassword(encoder.encode(request.getNewPassword())); users.save(user);
        return java.util.Map.of("message", "Password changed successfully");
    }

    @PostMapping({"/user/refresh-token", "/worker/refresh-token"})
    public java.util.Map<String,String> refreshToken(Authentication auth) {
        User user=current(auth);
        return java.util.Map.of("token", jwt.generateToken(user.getEmail(), user.getRole()));
    }

    @GetMapping("/user/notifications")
    public List<ComplaintStatusHistoryResponse> citizenNotifications(Authentication auth) {
        return complaints.findByUserEmail(auth.getName()).stream()
                .flatMap(c -> history.findByComplaintComplaintIdOrderByUpdatedAtAsc(c.getComplaintId()).stream()
                .map(h -> new ComplaintStatusHistoryResponse(h.getHistoryId(), c.getComplaintId(),
                        h.getOldStatus(), h.getNewStatus(), h.getUpdatedBy(), h.getRemarks(), h.getUpdatedAt()))
                )
                .sorted((a,b) -> b.getUpdatedAt().compareTo(a.getUpdatedAt())).limit(25).toList();
    }

    private User current(Authentication auth) { return users.findByEmail(auth.getName()).orElseThrow(() -> new IllegalArgumentException("Account not found")); }
    private UserProfileResponse profile(String email) { User u = users.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("Account not found")); return new UserProfileResponse(u.getUserId(), u.getName(), u.getEmail(), u.getRole()); }
}
