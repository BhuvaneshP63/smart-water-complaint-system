package com.swcms.controller;

import java.util.*;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import com.swcms.model.Assignment;
import com.swcms.repository.AssignmentRepository;
import com.swcms.repository.FeedbackRepository;
import com.swcms.repository.ComplaintStatusHistoryRepository;

@RestController
@RequestMapping("/api/worker")
public class WorkerController {
    private final AssignmentRepository assignments;
    private final FeedbackRepository citizenFeedback;
    private final ComplaintStatusHistoryRepository history;
    public WorkerController(AssignmentRepository assignments, FeedbackRepository citizenFeedback,
            ComplaintStatusHistoryRepository history) {
        this.assignments=assignments; this.citizenFeedback=citizenFeedback; this.history=history;
    }
    @GetMapping("/stats")
    @Transactional(readOnly=true)
    public Map<String,Object> stats(Authentication auth) {
        List<Assignment> rows=assignments.findAssignmentsByWorkerEmail(auth.getName());
        long resolved=rows.stream().filter(a -> "Resolved".equals(a.getComplaint().getStatus())).count();
        long active=rows.stream().filter(a -> !List.of("Resolved","Cancelled","Rejected").contains(a.getComplaint().getStatus())).count();
        List<Integer> ratings=citizenFeedback.findAllWithComplaintAndUser().stream()
                .filter(f -> rows.stream().anyMatch(a -> a.getComplaint().getComplaintId()==f.getComplaint().getComplaintId()))
                .map(f -> f.getRating()).toList();
        double average=ratings.stream().mapToInt(Integer::intValue).average().orElse(0);
        List<Map<String,Object>> completedWork=rows.stream().filter(a -> "Resolved".equals(a.getComplaint().getStatus()))
                .map(a -> {
                    var resolvedAt=history.findByComplaintComplaintIdOrderByUpdatedAtAsc(a.getComplaint().getComplaintId()).stream()
                            .filter(h -> "Resolved".equals(h.getNewStatus())).map(h -> h.getUpdatedAt()).reduce((first,second)->second).orElse(null);
                    Map<String,Object> row=new HashMap<>();
                    row.put("complaintId",a.getComplaint().getComplaintId()); row.put("complaintType",a.getComplaint().getComplaintType());
                    row.put("priority",a.getComplaint().getPriority()); row.put("status",a.getComplaint().getStatus()); row.put("resolvedAt",resolvedAt);
                    return row;
                }).toList();
        return Map.of("assigned",rows.size(),"active",active,"resolved",resolved,
                "citizenFeedbackAverage",average,"completedWork",completedWork);
    }
}
