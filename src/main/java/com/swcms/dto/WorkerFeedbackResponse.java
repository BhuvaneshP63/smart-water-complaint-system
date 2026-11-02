package com.swcms.dto;

import java.time.LocalDateTime;

public class WorkerFeedbackResponse {

    private Integer feedbackId;
    private Integer complaintId;
    private String workerName;
    private Integer rating;
    private String comment;
    private String photoPath;
    private LocalDateTime submittedAt;

    public WorkerFeedbackResponse(Integer feedbackId, Integer complaintId,
            String workerName, Integer rating, String comment,
            String photoPath, LocalDateTime submittedAt) {
        this.feedbackId = feedbackId;
        this.complaintId = complaintId;
        this.workerName = workerName;
        this.rating = rating;
        this.comment = comment;
        this.photoPath = photoPath;
        this.submittedAt = submittedAt;
    }

    public Integer getFeedbackId() {
        return feedbackId;
    }

    public Integer getComplaintId() {
        return complaintId;
    }

    public String getWorkerName() {
        return workerName;
    }

    public Integer getRating() {
        return rating;
    }

    public String getComment() {
        return comment;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }
}