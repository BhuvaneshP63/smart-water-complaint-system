package com.swcms.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import com.swcms.dto.*;
import com.swcms.model.*;
import com.swcms.repository.*;
import com.swcms.service.ComplaintService;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final UserRepository users;
    private final AssignmentRepository assignments;
    private final ComplaintRepository complaints;
    private final FeedbackRepository citizenFeedback;
    private final ComplaintService complaintService;
    private final ComplaintStatusHistoryRepository histories;

    public AdminController(UserRepository users, AssignmentRepository assignments,
            ComplaintRepository complaints,
            FeedbackRepository citizenFeedback, ComplaintService complaintService,
            ComplaintStatusHistoryRepository histories) {
        this.users=users; this.assignments=assignments; this.complaints=complaints;
        this.citizenFeedback=citizenFeedback; this.complaintService=complaintService; this.histories=histories;
    }

    @GetMapping("/test")
    public String adminTest(Authentication authentication) { return "Welcome ADMIN: " + authentication.getName(); }

    @GetMapping("/workers")
    @Transactional(readOnly=true)
    public List<WorkerManagementResponse> workers() {
        return users.findByRole("WORKER").stream().map(worker -> {
            List<Assignment> workerAssignments = assignments.findByWorkerUserId(worker.getUserId());
            int activeCount = (int) workerAssignments.stream().filter(a -> !List.of("Resolved","Cancelled","Rejected").contains(a.getComplaint().getStatus())).count();
            int resolved = (int) workerAssignments.stream().filter(a -> "Resolved".equals(a.getComplaint().getStatus())).count();
            var complaintIds = workerAssignments.stream().map(a -> a.getComplaint().getComplaintId()).collect(java.util.stream.Collectors.toSet());
            var ratings = citizenFeedback.findAllWithComplaintAndUser().stream()
                    .filter(f -> complaintIds.contains(f.getComplaint().getComplaintId())).mapToInt(com.swcms.model.Feedback::getRating).toArray();
            double average = ratings.length == 0 ? 0 : java.util.Arrays.stream(ratings).average().orElse(0);
            return new WorkerManagementResponse(worker.getUserId(), worker.getName(), worker.getEmail(), worker.isActive(), activeCount, resolved, average);
        }).toList();
    }

    @PutMapping("/workers/{id}")
    @Transactional
    public WorkerManagementResponse updateWorker(@PathVariable Integer id, @Valid @RequestBody WorkerUpdateRequest request) {
        User worker = users.findById(id).filter(u -> "WORKER".equals(u.getRole()))
                .orElseThrow(() -> new IllegalArgumentException("Worker not found"));
        if (!worker.getEmail().equalsIgnoreCase(request.getEmail()) && users.existsByEmail(request.getEmail()))
            throw new IllegalArgumentException("Email is already registered");
        worker.setName(request.getName().trim()); worker.setEmail(request.getEmail().trim().toLowerCase());
        if (request.getActive() != null) worker.setActive(request.getActive());
        users.save(worker);
        List<Assignment> assignmentsForWorker = assignments.findByWorkerUserId(id);
        int activeCount=(int)assignmentsForWorker.stream().filter(a -> !List.of("Resolved","Cancelled","Rejected").contains(a.getComplaint().getStatus())).count();
        int resolved=(int)assignmentsForWorker.stream().filter(a -> "Resolved".equals(a.getComplaint().getStatus())).count();
        var complaintIds=assignmentsForWorker.stream().map(a -> a.getComplaint().getComplaintId()).collect(java.util.stream.Collectors.toSet());
        double avg=citizenFeedback.findAllWithComplaintAndUser().stream().filter(f -> complaintIds.contains(f.getComplaint().getComplaintId())).mapToInt(com.swcms.model.Feedback::getRating).average().orElse(0);
        return new WorkerManagementResponse(id,worker.getName(),worker.getEmail(),worker.isActive(),activeCount,resolved,avg);
    }

    @GetMapping("/workers/{id}/complaints")
    @Transactional(readOnly=true)
    public List<Map<String,Object>> workerComplaints(@PathVariable Integer id) {
        return assignments.findByWorkerUserId(id).stream().map(a -> {
            Complaint c=a.getComplaint(); Map<String,Object> item=new java.util.HashMap<>();
            item.put("complaintId",c.getComplaintId()); item.put("customerName",c.getCustomerName());
            item.put("complaintType",c.getComplaintType()); item.put("status",c.getStatus());
            item.put("priority",c.getPriority()); item.put("assignedAt",a.getAssignedAt()); return item;
        }).toList();
    }

    @GetMapping("/complaints/overdue")
    public List<Complaint> overdueComplaints() {
        LocalDateTime cutoff=LocalDateTime.now().minusDays(7);
        return complaints.findAllByOrderByComplaintIdDesc().stream().filter(c -> c.getCreatedAt()!=null && c.getCreatedAt().isBefore(cutoff)
                && !List.of("Resolved","Cancelled","Rejected").contains(c.getStatus())).toList();
    }

    @PostMapping("/complaints/escalate-overdue")
    public Map<String,Integer> escalateOverdue() { return Map.of("escalated", complaintService.escalateOverdue()); }

    @GetMapping("/activity")
    @Transactional(readOnly=true)
    public List<ComplaintStatusHistoryResponse> recentActivity() {
        return histories.findTop20ByOrderByUpdatedAtDesc().stream().map(h -> new ComplaintStatusHistoryResponse(
                h.getHistoryId(), h.getComplaint().getComplaintId(), h.getOldStatus(), h.getNewStatus(),
                h.getUpdatedBy(), h.getRemarks(), h.getUpdatedAt())).toList();
    }
}
