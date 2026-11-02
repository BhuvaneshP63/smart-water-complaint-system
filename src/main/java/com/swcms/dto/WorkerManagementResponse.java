package com.swcms.dto;

public class WorkerManagementResponse {
    private final Integer userId;
    private final String name;
    private final String email;
    private final boolean active;
    private final int assignedCount;
    private final int resolvedCount;
    private final double averageRating;
    public WorkerManagementResponse(Integer userId, String name, String email,
            boolean active, int assignedCount, int resolvedCount, double averageRating) {
        this.userId = userId; this.name = name; this.email = email;
        this.active = active; this.assignedCount = assignedCount;
        this.resolvedCount = resolvedCount; this.averageRating = averageRating;
    }
    public Integer getUserId() { return userId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public boolean isActive() { return active; }
    public int getAssignedCount() { return assignedCount; }
    public int getResolvedCount() { return resolvedCount; }
    public double getAverageRating() { return averageRating; }
}
