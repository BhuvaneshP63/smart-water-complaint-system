package com.swcms.dto;

import java.time.LocalDateTime;

public class ComplaintStatusHistoryResponse {

    private int historyId;
    private int complaintId;
    private String oldStatus;
    private String newStatus;
    private String updatedBy;
    private String remarks;
    private LocalDateTime updatedAt;

    public ComplaintStatusHistoryResponse(
            int historyId,
            int complaintId,
            String oldStatus,
            String newStatus,
            String updatedBy,
            String remarks,
            LocalDateTime updatedAt) {

        this.historyId = historyId;
        this.complaintId = complaintId;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.updatedBy = updatedBy;
        this.remarks = remarks;
        this.updatedAt = updatedAt;
    }

    public int getHistoryId() {
        return historyId;
    }

    public int getComplaintId() {
        return complaintId;
    }

    public String getOldStatus() {
        return oldStatus;
    }

    public String getNewStatus() {
        return newStatus;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public String getRemarks() {
        return remarks;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}