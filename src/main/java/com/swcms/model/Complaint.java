
package com.swcms.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
@Table(name = "complaints", indexes = {
        @Index(name = "idx_complaint_status", columnList = "status"),
        @Index(name = "idx_complaint_priority", columnList = "priority"),
        @Index(name = "idx_complaint_created", columnList = "created_at"),
        @Index(name = "idx_complaint_user", columnList = "user_id")
})
public class Complaint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int complaintId;

    @NotBlank(message = "Customer name is required")
    @Size(max = 100, message = "Customer name cannot exceed 100 characters")
    private String customerName;

    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "Complaint type is required")
    private String complaintType;

    @NotBlank(message = "Description is required")
    @Column(length = 1000)
    private String description;

    private String status;

    @Column(nullable = false)
    private String priority = "Medium";

    // Citizen complaint image
    @Column(name = "photo_path", length = 500)
    private String photoPath;

    // Worker completion image
    @Column(name = "completion_photo_path", length = 500)
    private String completionPhotoPath;

    // Work completion remarks by worker
    @Column(name = "completion_remarks", length = 1000)
    private String completionRemarks;
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    public Complaint() {
    }

    public Complaint(int complaintId, String customerName,
                     String address, String complaintType,
                     String description) {

        this.complaintId = complaintId;
        this.customerName = customerName;
        this.address = address;
        this.complaintType = complaintType;
        this.description = description;
        this.status = "Pending";
        this.priority = "Medium";
    }

    @PrePersist
    public void setDefaultValues() {

        if (this.createdAt == null) this.createdAt = LocalDateTime.now();

        if (this.status == null || this.status.isBlank()) {
            this.status = "Pending";
        }

        if (this.priority == null || this.priority.isBlank()) {
            this.priority = "Medium";
        }
    }

    // Getters

    public int getComplaintId() {
        return complaintId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getAddress() {
        return address;
    }

    public String getComplaintType() {
        return complaintType;
    }

    public String getDescription() {
        return description;
    }

    public String getStatus() {
        return status;
    }

    public String getPriority() {
        return priority;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public String getCompletionPhotoPath() {
        return completionPhotoPath;
    }

    public String getCompletionRemarks() {
        return completionRemarks;
    }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public User getUser() {
        return user;
    }

    // Setters

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setComplaintType(String complaintType) {
        this.complaintType = complaintType;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }

    public void setCompletionPhotoPath(String completionPhotoPath) {
        this.completionPhotoPath = completionPhotoPath;
    }

    public void setCompletionRemarks(String completionRemarks) {
        this.completionRemarks = completionRemarks;
    }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public void setUser(User user) {
        this.user = user;
    }
}
