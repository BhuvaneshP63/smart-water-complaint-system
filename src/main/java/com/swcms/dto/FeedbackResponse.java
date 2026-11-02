
package com.swcms.dto;

import java.time.LocalDateTime;

public class FeedbackResponse {

    private Integer feedbackId;
    private Integer complaintId;
    private String customerName;
    private String citizenEmail;
    private Integer rating;
    private String comment;
    private String photoPath;
    private LocalDateTime submittedAt;

    public FeedbackResponse(
            Integer feedbackId,
            Integer complaintId,
            String customerName,
            String citizenEmail,
            Integer rating,
            String comment,
            String photoPath,
            LocalDateTime submittedAt) {

        this.feedbackId = feedbackId;
        this.complaintId = complaintId;
        this.customerName = customerName;
        this.citizenEmail = citizenEmail;
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

    public String getCustomerName() {
        return customerName;
    }

    public String getCitizenEmail() {
        return citizenEmail;
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
