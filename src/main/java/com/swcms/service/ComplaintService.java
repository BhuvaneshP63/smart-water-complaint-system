
package com.swcms.service;

import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.swcms.dto.ComplaintRequest;
import com.swcms.dto.ComplaintStatusHistoryResponse;
import com.swcms.model.Assignment;
import com.swcms.model.Complaint;
import com.swcms.model.ComplaintStatusHistory;
import com.swcms.model.User;
import com.swcms.repository.AssignmentRepository;
import com.swcms.repository.ComplaintRepository;
import com.swcms.repository.ComplaintStatusHistoryRepository;
import com.swcms.repository.UserRepository;

@Service
public class ComplaintService {

    private final ComplaintRepository repository;
    private final UserRepository userRepository;
    private final AssignmentRepository assignmentRepository;
    private final ComplaintStatusHistoryRepository historyRepository;
    private final FileStorageService fileStorageService;

    public ComplaintService(
            ComplaintRepository repository,
            UserRepository userRepository,
            AssignmentRepository assignmentRepository,
            ComplaintStatusHistoryRepository historyRepository,
            FileStorageService fileStorageService) {

        this.repository = repository;
        this.userRepository = userRepository;
        this.assignmentRepository = assignmentRepository;
        this.historyRepository = historyRepository;
        this.fileStorageService = fileStorageService;
    }

    // Helper: Save complaint status history
    private void saveStatusHistory(
            Complaint complaint,
            String oldStatus,
            String newStatus,
            String updatedBy,
            String remarks) {

        if (oldStatus != null && oldStatus.equals(newStatus)
                && (remarks == null || (!remarks.startsWith("Escalated") && !remarks.startsWith("Reassigned")))) {
            return;
        }

        ComplaintStatusHistory history = new ComplaintStatusHistory();
        history.setComplaint(complaint);
        history.setOldStatus(oldStatus);
        history.setNewStatus(newStatus);
        history.setUpdatedBy(updatedBy);
        history.setRemarks(remarks);

        historyRepository.save(history);
    }

    // 1. Add complaint with optional photo
    @Transactional
    public Complaint addComplaint(ComplaintRequest request, String email) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Complaint request cannot be null");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        Complaint complaint = new Complaint();
        complaint.setCustomerName(request.getCustomerName());
        complaint.setAddress(request.getAddress());
        complaint.setComplaintType(request.getComplaintType());
        complaint.setDescription(request.getDescription());
        complaint.setPriority(request.getPriority());
        complaint.setUser(user);

        // Save photo only when the citizen attaches one
        if (request.getPhoto() != null &&
                !request.getPhoto().isEmpty()) {

            String photoPath = fileStorageService.storeFile(
                    request.getPhoto(), "complaints");

            complaint.setPhotoPath(photoPath);
        }

        Complaint savedComplaint = repository.save(complaint);

        // Record initial status
        saveStatusHistory(
                savedComplaint,
                "Not Created",
                savedComplaint.getStatus(),
                email,
                "Complaint submitted by citizen"
        );

        return savedComplaint;
    }

    // 2. Get all complaints (Admin)
    public List<Complaint> getAllComplaints() {
        return repository.findAllByOrderByComplaintIdDesc();
    }

    public org.springframework.data.domain.Page<Complaint> getAllComplaintsPage(
            org.springframework.data.domain.Pageable pageable) {
        return repository.findAll(pageable);
    }

    // 3. Get complaints by citizen email
    public List<Complaint> getUserComplaints(String email) {
        return repository.findByUserEmail(email);
    }

    // 4. Get complaint by ID
    public Complaint getComplaintById(int id) {
        return repository.findById(id).orElse(null);
    }

    // 5. Get complaint only if it belongs to citizen
    @Transactional(readOnly = true)
    public Complaint getUserComplaintById(int id, String email) {

        return repository.findById(id)
                .filter(complaint ->
                        complaint.getUser() != null &&
                        complaint.getUser().getEmail().equals(email))
                .orElse(null);
    }

    // 6. Update complaint status (Admin)
    @Transactional
    public boolean updateStatus(int id, String status) {

        Complaint complaint = getComplaintById(id);

        if (complaint == null) {
            return false;
        }

        if (status == null ||
                !List.of("Pending", "Assigned",
                        "In Progress", "Resolved").contains(status)) {

            throw new IllegalArgumentException(
                    "Invalid complaint status");
        }

        String oldStatus = complaint.getStatus();
        if (!isValidTransition(oldStatus, status)) {
            throw new IllegalArgumentException("Status cannot change from " + oldStatus + " to " + status);
        }
        if ("Assigned".equals(status) && assignmentRepository.findByComplaintComplaintId(id).isEmpty()) {
            throw new IllegalArgumentException("Assign the complaint to a worker before marking it assigned");
        }
        complaint.setStatus(status);
        repository.save(complaint);

        saveStatusHistory(
                complaint,
                oldStatus,
                status,
                "ADMIN",
                "Status updated by admin"
        );

        return true;
    }

    // 7. Update complaint priority (Admin)
    @Transactional
    public boolean updatePriority(int id, String priority) {

        Complaint complaint = getComplaintById(id);

        if (complaint == null) {
            return false;
        }

        if (priority == null ||
                !List.of("Low", "Medium", "High").contains(priority)) {

            throw new IllegalArgumentException(
                    "Invalid complaint priority");
        }

        complaint.setPriority(priority);
        repository.save(complaint);

        return true;
    }

    // 8. Delete complaint (Admin)
    @Transactional
    public boolean deleteComplaint(int id) {

        if (!repository.existsById(id)) {
            return false;
        }

        historyRepository.deleteAll(
                historyRepository
                        .findByComplaintComplaintIdOrderByUpdatedAtAsc(id)
        );

        assignmentRepository.deleteByComplaintComplaintId(id);
        repository.deleteById(id);

        return true;
    }

    // 9. Assign complaint to Worker (Admin)
    @Transactional
    public boolean assignComplaint(int complaintId, String workerEmail) {

        Complaint complaint = getComplaintById(complaintId);

        if (complaint == null) {
            return false;
        }

        User worker = userRepository.findByEmail(workerEmail)
                .orElseThrow(() ->
                        new IllegalArgumentException("Worker not found"));

        if (!"WORKER".equals(worker.getRole())) {
            throw new IllegalArgumentException(
                    "Selected user is not a Worker");
        }
        if (!worker.isActive()) throw new IllegalArgumentException("Selected worker account is inactive");
        if (List.of("Resolved", "Cancelled", "Rejected").contains(complaint.getStatus()))
            throw new IllegalArgumentException("Closed complaints cannot be assigned");

        Assignment assignment = assignmentRepository
                .findByComplaintComplaintId(complaintId)
                .orElse(new Assignment());
        boolean wasAssigned = assignment.getAssignmentId() != null;
        String previousWorker = wasAssigned && assignment.getWorker() != null ? assignment.getWorker().getEmail() : null;

        assignment.setComplaint(complaint);
        assignment.setWorker(worker);
        assignment.setAssignedAt(LocalDateTime.now());
        assignmentRepository.save(assignment);

        String oldStatus = complaint.getStatus();
        complaint.setStatus("Assigned");
        repository.save(complaint);

        saveStatusHistory(
                complaint,
                oldStatus,
                "Assigned",
                "ADMIN",
                wasAssigned ? "Reassigned from " + previousWorker + " to " + workerEmail
                        : "Complaint assigned to worker: " + workerEmail
        );

        return true;
    }

    // 10. Get complaints assigned to Worker
    @Transactional(readOnly = true)
    public List<Complaint> getWorkerComplaints(String workerEmail) {

        return assignmentRepository
                .findAssignmentsByWorkerEmail(workerEmail)
                .stream()
                .map(Assignment::getComplaint)
                .collect(Collectors.toList());
    }

    // 11. Worker updates assigned complaint status with completion photo
    @Transactional
    public boolean updateWorkerComplaintStatus(
            int complaintId,
            String status,
            String workerEmail,
            MultipartFile completionPhoto,
            String completionRemarks) {

        if (status == null ||
                !List.of("In Progress", "Resolved").contains(status)) {

            throw new IllegalArgumentException(
                    "Worker can only update status to In Progress or Resolved");
        }

        boolean assigned = assignmentRepository
                .existsByComplaintComplaintIdAndWorkerEmail(
                        complaintId, workerEmail);

        if (!assigned) {
            return false;
        }

        Complaint complaint = getComplaintById(complaintId);

        if (complaint == null) {
            return false;
        }

        if (!isValidTransition(complaint.getStatus(), status))
            throw new IllegalArgumentException("Status cannot change from " + complaint.getStatus() + " to " + status);

        // Completion photo is compulsory when resolving
        if ("Resolved".equals(status)) {

            if (completionPhoto == null ||
                    completionPhoto.isEmpty()) {

                throw new IllegalArgumentException(
                        "Completion photo is required to resolve a complaint");
            }

            String photoPath = fileStorageService.storeFile(
                    completionPhoto, "completion");

            complaint.setCompletionPhotoPath(photoPath);
            complaint.setCompletionRemarks(completionRemarks);
        }

        String oldStatus = complaint.getStatus();
        complaint.setStatus(status);

        repository.save(complaint);

        saveStatusHistory(
                complaint,
                oldStatus,
                status,
                workerEmail,
                "Resolved".equals(status)
                        ? "Complaint completed. " +
                          (completionRemarks == null
                                  ? ""
                                  : completionRemarks)
                        : "Status updated by assigned worker"
        );

        return true;
    }

    private boolean isValidTransition(String oldStatus, String newStatus) {
        if (oldStatus != null && oldStatus.equals(newStatus)) return true;
        return switch (oldStatus == null ? "Pending" : oldStatus) {
            case "Pending" -> List.of("Assigned", "Rejected", "Cancelled").contains(newStatus);
            case "Assigned" -> List.of("In Progress", "Pending", "Cancelled", "Rejected").contains(newStatus);
            case "In Progress" -> List.of("Resolved", "Assigned", "Cancelled", "Rejected").contains(newStatus);
            default -> false;
        };
    }

    @Transactional
    public int escalateOverdue() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(7);
        int count = 0;
        for (Complaint complaint : repository.findAllByOrderByComplaintIdDesc()) {
            if (complaint.getCreatedAt() != null && complaint.getCreatedAt().isBefore(cutoff)
                    && !List.of("Resolved", "Cancelled", "Rejected").contains(complaint.getStatus())
                    && !"High".equals(complaint.getPriority())) {
                complaint.setPriority("High");
                repository.save(complaint);
                saveStatusHistory(complaint, complaint.getStatus(), complaint.getStatus(), "ADMIN",
                        "Escalated automatically: complaint overdue by more than 7 days");
                count++;
            }
        }
        return count;
    }

    // 12. Get complaint status history
    @Transactional(readOnly = true)
    public List<ComplaintStatusHistoryResponse> getComplaintHistory(
            int complaintId) {

        return historyRepository
                .findByComplaintComplaintIdOrderByUpdatedAtAsc(complaintId)
                .stream()
                .map(history -> new ComplaintStatusHistoryResponse(
                        history.getHistoryId(),
                        complaintId,
                        history.getOldStatus(),
                        history.getNewStatus(),
                        history.getUpdatedBy(),
                        history.getRemarks(),
                        history.getUpdatedAt()
                ))
                .collect(Collectors.toList());
    }
}
