
package com.swcms.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "complaint_status_history", indexes = {
        @Index(name = "idx_history_complaint_time", columnList = "complaint_id,updated_at")
})
public class ComplaintStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int historyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "complaint_id", nullable = false)
    private Complaint complaint;

    @Column(nullable = false)
    private String oldStatus;

    @Column(nullable = false)
    private String newStatus;

    private String updatedBy;

    @Column(length = 500)
    private String remarks;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public ComplaintStatusHistory() {
    }

    @PrePersist
    public void setTimestamp() {
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }

    public int getHistoryId() {
        return historyId;
    }

    public Complaint getComplaint() {
        return complaint;
    }

    public void setComplaint(Complaint complaint) {
        this.complaint = complaint;
    }

    public String getOldStatus() {
        return oldStatus;
    }

    public void setOldStatus(String oldStatus) {
        this.oldStatus = oldStatus;
    }

    public String getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(String newStatus) {
        this.newStatus = newStatus;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }	

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
