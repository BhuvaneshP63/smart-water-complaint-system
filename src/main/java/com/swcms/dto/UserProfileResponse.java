package com.swcms.dto;

public class UserProfileResponse {
    private final Integer userId;
    private final String name;
    private final String email;
    private final String role;
    public UserProfileResponse(Integer userId, String name, String email, String role) {
        this.userId = userId; this.name = name; this.email = email; this.role = role;
    }
    public Integer getUserId() { return userId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
}
