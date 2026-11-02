
package com.swcms.service;

import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.swcms.dto.FeedbackRequest;
import com.swcms.dto.FeedbackResponse;
import com.swcms.model.Complaint;
import com.swcms.model.Feedback;
import com.swcms.repository.AssignmentRepository;
import com.swcms.repository.ComplaintRepository;
import com.swcms.repository.FeedbackRepository;

@Service
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final ComplaintRepository complaintRepository;
    private final FileStorageService fileStorageService;
    private final AssignmentRepository assignmentRepository;

    public FeedbackService(
            FeedbackRepository feedbackRepository,
            ComplaintRepository complaintRepository,
            FileStorageService fileStorageService,
            AssignmentRepository assignmentRepository) {

        this.feedbackRepository = feedbackRepository;
        this.complaintRepository = complaintRepository;
        this.fileStorageService = fileStorageService;
        this.assignmentRepository = assignmentRepository;
    }

    // 1. Submit feedback with optional photo
    @Transactional
    public FeedbackResponse submitFeedback(
            Integer complaintId,
            FeedbackRequest request,
            String citizenEmail) {

        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Complaint not found"));

        if (complaint.getUser() == null ||
                !complaint.getUser().getEmail().equals(citizenEmail)) {

            throw new IllegalArgumentException(
                    "You cannot submit feedback for another citizen's complaint");
        }

        if (!"Resolved".equals(complaint.getStatus())) {
            throw new IllegalArgumentException(
                    "Feedback can only be submitted for resolved complaints");
        }

        if (feedbackRepository.existsByComplaintComplaintId(complaintId)) {
            throw new IllegalArgumentException(
                    "Feedback has already been submitted for this complaint");
        }

        Feedback feedback = new Feedback();

        feedback.setComplaint(complaint);
        feedback.setRating(request.getRating());
        feedback.setComment(request.getComment());

        if (request.getPhoto() != null &&
                !request.getPhoto().isEmpty()) {

            String photoPath = fileStorageService.storeFile(
                    request.getPhoto(), "feedback");

            feedback.setPhotoPath(photoPath);
        }

        Feedback saved = feedbackRepository.save(feedback);

        return toResponse(saved);
    }

    // 2. Get feedback submitted by a citizen
    @Transactional(readOnly = true)
    public List<FeedbackResponse> getCitizenFeedback(String email) {

        return feedbackRepository.findByCitizenEmail(email)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // 3. Get all feedback (Admin)
    @Transactional(readOnly = true)
    public List<FeedbackResponse> getAllFeedback() {

        return feedbackRepository.findAllWithComplaintAndUser()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // 4. Get feedback photo with role-based authorization
    @Transactional(readOnly = true)
    public Resource getFeedbackPhoto(
            Integer feedbackId,
            String email,
            boolean isAdmin,
            boolean isWorker) {

        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Feedback not found"));

        Complaint complaint = feedback.getComplaint();

        // Admin can view all feedback photos
        if (!isAdmin) {

            // Citizen can view their own complaint's feedback photo
            boolean isCitizenOwner =
                    complaint.getUser() != null &&
                    complaint.getUser().getEmail().equals(email);

            // Worker can view feedback photos only for assigned complaints
            boolean isAssignedWorker = isWorker &&
                    assignmentRepository
                            .existsByComplaintComplaintIdAndWorkerEmail(
                                    complaint.getComplaintId(), email);

            if (!isCitizenOwner && !isAssignedWorker) {
                throw new IllegalArgumentException(
                        "You are not authorized to view this photo");
            }
        }

        if (feedback.getPhotoPath() == null ||
                feedback.getPhotoPath().isBlank()) {

            throw new IllegalArgumentException(
                    "Feedback photo not found");
        }

        return fileStorageService.loadFile(feedback.getPhotoPath());
    }

    // 5. Convert Feedback entity to response DTO
    private FeedbackResponse toResponse(Feedback feedback) {

        Complaint complaint = feedback.getComplaint();

        return new FeedbackResponse(
                feedback.getFeedbackId(),
                complaint.getComplaintId(),
                complaint.getCustomerName(),
                complaint.getUser().getEmail(),
                feedback.getRating(),
                feedback.getComment(),
                feedback.getPhotoPath(),
                feedback.getSubmittedAt()
        );
    }
 // 6. Worker views citizen feedback for assigned complaints
    @Transactional(readOnly = true)
    public List<FeedbackResponse> getWorkerFeedback(String workerEmail) {

        return feedbackRepository.findAllWithComplaintAndUser()
                .stream()
                .filter(feedback ->
                        assignmentRepository
                                .existsByComplaintComplaintIdAndWorkerEmail(
                                        feedback.getComplaint().getComplaintId(),
                                        workerEmail))
                .map(this::toResponse)
                .toList();
    }
}
