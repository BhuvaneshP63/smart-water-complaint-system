
package com.swcms.service;

import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.swcms.dto.WorkerFeedbackRequest;
import com.swcms.dto.WorkerFeedbackResponse;
import com.swcms.model.Complaint;
import com.swcms.model.User;
import com.swcms.model.WorkerFeedback;
import com.swcms.repository.AssignmentRepository;
import com.swcms.repository.ComplaintRepository;
import com.swcms.repository.UserRepository;
import com.swcms.repository.WorkerFeedbackRepository;

@Service
public class WorkerFeedbackService {

    private final WorkerFeedbackRepository feedbackRepository;
    private final ComplaintRepository complaintRepository;
    private final AssignmentRepository assignmentRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    public WorkerFeedbackService(
            WorkerFeedbackRepository feedbackRepository,
            ComplaintRepository complaintRepository,
            AssignmentRepository assignmentRepository,
            UserRepository userRepository,
            FileStorageService fileStorageService) {

        this.feedbackRepository = feedbackRepository;
        this.complaintRepository = complaintRepository;
        this.assignmentRepository = assignmentRepository;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
    }

    // 1. Worker submits feedback after resolving complaint
    @Transactional
    public WorkerFeedbackResponse submitFeedback(
            Integer complaintId,
            WorkerFeedbackRequest request,
            String workerEmail) {

        boolean assigned = assignmentRepository
                .existsByComplaintComplaintIdAndWorkerEmail(
                        complaintId, workerEmail);

        if (!assigned) {
            throw new IllegalArgumentException(
                    "You are not assigned to this complaint");
        }

        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Complaint not found"));

        if (!"Resolved".equals(complaint.getStatus())) {
            throw new IllegalArgumentException(
                    "Feedback can only be submitted after resolving the complaint");
        }

        if (feedbackRepository.existsByComplaintComplaintId(complaintId)) {
            throw new IllegalArgumentException(
                    "Worker feedback has already been submitted");
        }

        User worker = userRepository.findByEmail(workerEmail)
                .orElseThrow(() ->
                        new IllegalArgumentException("Worker not found"));

        if (!"WORKER".equals(worker.getRole())) {
            throw new IllegalArgumentException(
                    "Only workers can submit worker feedback");
        }

        WorkerFeedback feedback = new WorkerFeedback();

        feedback.setComplaint(complaint);
        feedback.setWorker(worker);
        feedback.setRating(request.getRating());
        feedback.setComment(request.getComment());

        MultipartFile photo = request.getPhoto();

        if (photo != null && !photo.isEmpty()) {
            String photoPath = fileStorageService.storeFile(
                    photo, "worker-feedback");

            feedback.setPhotoPath(photoPath);
        }

        WorkerFeedback saved = feedbackRepository.save(feedback);

        return toResponse(saved);
    }

    // 2. Citizen views worker feedback for their complaint
    @Transactional(readOnly = true)
    public WorkerFeedbackResponse getCitizenWorkerFeedback(
            Integer complaintId, String citizenEmail) {

        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Complaint not found"));

        if (complaint.getUser() == null ||
                !complaint.getUser().getEmail().equals(citizenEmail)) {

            throw new IllegalArgumentException(
                    "You cannot view another citizen's complaint feedback");
        }

        WorkerFeedback feedback = feedbackRepository
                .findByComplaintComplaintId(complaintId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Worker feedback not found"));

        return toResponse(feedback);
    }

    // 3. Citizen views worker feedback photo
    @Transactional(readOnly = true)
    public Resource getCitizenWorkerFeedbackPhoto(
            Integer complaintId, String citizenEmail) {

        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Complaint not found"));

        if (complaint.getUser() == null ||
                !complaint.getUser().getEmail().equals(citizenEmail)) {

            throw new IllegalArgumentException(
                    "You cannot access another citizen's complaint photo");
        }

        WorkerFeedback feedback = feedbackRepository
                .findByComplaintComplaintId(complaintId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Worker feedback not found"));

        if (feedback.getPhotoPath() == null ||
                feedback.getPhotoPath().isBlank()) {

            throw new IllegalArgumentException(
                    "No worker feedback photo available");
        }

        return fileStorageService.loadFile(feedback.getPhotoPath());
    }

    // 4. Worker checks their submitted feedback
    @Transactional(readOnly = true)
    public WorkerFeedbackResponse getWorkerFeedback(
            Integer complaintId, String workerEmail) {

        boolean assigned = assignmentRepository
                .existsByComplaintComplaintIdAndWorkerEmail(
                        complaintId, workerEmail);

        if (!assigned) {
            throw new IllegalArgumentException(
                    "You are not assigned to this complaint");
        }

        WorkerFeedback feedback = feedbackRepository
                .findByComplaintComplaintId(complaintId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Worker feedback not submitted yet"));

        if (!feedback.getWorker().getEmail().equals(workerEmail)) {
            throw new IllegalArgumentException(
                    "You cannot view another worker's feedback");
        }

        return toResponse(feedback);
    }

    @Transactional(readOnly = true)
    public List<WorkerFeedbackResponse> getAllWorkerFeedback() {
        return feedbackRepository.findAllByOrderBySubmittedAtDesc().stream()
                .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public Resource getFeedbackPhoto(Integer feedbackId, String email,
            boolean isAdmin, boolean isWorker) {
        WorkerFeedback feedback = feedbackRepository.findByFeedbackId(feedbackId)
                .orElseThrow(() -> new IllegalArgumentException("Worker feedback not found"));
        if (!isAdmin && !(isWorker && assignmentRepository
                .existsByComplaintComplaintIdAndWorkerEmail(
                        feedback.getComplaint().getComplaintId(), email))) {
            throw new IllegalArgumentException("You are not authorized to view this photo");
        }
        if (feedback.getPhotoPath() == null || feedback.getPhotoPath().isBlank()) {
            throw new IllegalArgumentException("No worker feedback photo available");
        }
        return fileStorageService.loadFile(feedback.getPhotoPath());
    }

    // 5. Convert entity into response DTO
    private WorkerFeedbackResponse toResponse(
            WorkerFeedback feedback) {

        return new WorkerFeedbackResponse(
                feedback.getFeedbackId(),
                feedback.getComplaint().getComplaintId(),
                feedback.getWorker().getName(),
                feedback.getRating(),
                feedback.getComment(),
                feedback.getPhotoPath(),
                feedback.getSubmittedAt()
        );
    }
}
