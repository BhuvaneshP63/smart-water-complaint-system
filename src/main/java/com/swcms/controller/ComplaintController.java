
package com.swcms.controller;

import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.swcms.dto.ComplaintRequest;
import com.swcms.dto.ComplaintStatusHistoryResponse;
import com.swcms.dto.PageResponse;
import com.swcms.model.Complaint;
import com.swcms.service.ComplaintService;
import com.swcms.service.FileStorageService;

import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173")
public class ComplaintController {

    private final ComplaintService service;
    private final FileStorageService fileStorageService;

    public ComplaintController(
            ComplaintService service,
            FileStorageService fileStorageService) {
        this.service = service;
        this.fileStorageService = fileStorageService;
    }

    // USER: Submit a complaint with optional photo
    @PostMapping(
        value = "/user/complaints",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<Complaint> addComplaint(
            @Valid @ModelAttribute ComplaintRequest request,
            Authentication authentication) {

        Complaint saved = service.addComplaint(
                request, authentication.getName());

        return ResponseEntity.status(201).body(saved);
    }

    // USER: View own complaints
    @GetMapping("/user/complaints")
    public ResponseEntity<List<Complaint>> getUserComplaints(
            Authentication authentication) {

        return ResponseEntity.ok(
                service.getUserComplaints(authentication.getName()));
    }

    // ADMIN: View all complaints
    @GetMapping("/admin/complaints")
    public ResponseEntity<List<Complaint>> getAllComplaints() {
        return ResponseEntity.ok(service.getAllComplaints());
    }

    @GetMapping("/admin/complaints/page")
    public PageResponse<Complaint> getAdminComplaintsPage(
            @RequestParam(defaultValue="0") int page,
            @RequestParam(defaultValue="20") int size,
            @RequestParam(defaultValue="createdAt") String sortBy,
            @RequestParam(defaultValue="desc") String direction) {
        if (!List.of("createdAt","priority","status","complaintId").contains(sortBy))
            throw new IllegalArgumentException("Unsupported sort field");
        int safeSize=Math.max(1,Math.min(size,100));
        Sort sort=Sort.by("asc".equalsIgnoreCase(direction)?Sort.Direction.ASC:Sort.Direction.DESC,sortBy);
        return PageResponse.from(service.getAllComplaintsPage(PageRequest.of(Math.max(0,page),safeSize,sort)));
    }

    // ADMIN: View complaint by ID
    @GetMapping("/admin/complaints/{id}")
    public ResponseEntity<Complaint> getComplaintById(
            @PathVariable int id) {

        Complaint complaint = service.getComplaintById(id);

        if (complaint == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(complaint);
    }

    // ADMIN: Update complaint status
    @PutMapping("/admin/complaints/{id}/status")
    public ResponseEntity<String> updateStatus(
            @PathVariable int id,
            @RequestParam String status) {

        boolean updated = service.updateStatus(id, status);

        if (updated) {
            return ResponseEntity.ok(
                    "Complaint status updated successfully");
        }

        return ResponseEntity.notFound().build();
    }

    // ADMIN: Update complaint priority
    @PutMapping("/admin/complaints/{id}/priority")
    public ResponseEntity<String> updatePriority(
            @PathVariable int id,
            @RequestParam String priority) {

        boolean updated = service.updatePriority(id, priority);

        if (updated) {
            return ResponseEntity.ok(
                    "Complaint priority updated successfully");
        }

        return ResponseEntity.notFound().build();
    }

    // ADMIN: Delete complaint
    @DeleteMapping("/admin/complaints/{id}")
    public ResponseEntity<String> deleteComplaint(
            @PathVariable int id) {

        boolean deleted = service.deleteComplaint(id);

        if (deleted) {
            return ResponseEntity.ok(
                    "Complaint deleted successfully");
        }

        return ResponseEntity.notFound().build();
    }

    // ADMIN: Assign complaint to Worker
    @PutMapping("/admin/complaints/{id}/assign")
    public ResponseEntity<String> assignComplaint(
            @PathVariable int id,
            @RequestParam String workerEmail) {

        boolean assigned = service.assignComplaint(id, workerEmail);

        if (assigned) {
            return ResponseEntity.ok(
                    "Complaint assigned to Worker successfully");
        }

        return ResponseEntity.notFound().build();
    }

    // WORKER: View assigned complaints
    @GetMapping("/worker/complaints")
    public ResponseEntity<List<Complaint>> getWorkerComplaints(
            Authentication authentication) {

        return ResponseEntity.ok(
                service.getWorkerComplaints(authentication.getName()));
    }

    // WORKER: Update status and upload completion photo
    @PutMapping(
        value = "/worker/complaints/{id}/status",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<String> updateWorkerComplaintStatus(
            @PathVariable int id,
            @RequestParam String status,
            @RequestParam(required = false) MultipartFile photo,
            @RequestParam(required = false) String remarks,
            Authentication authentication) {

        boolean updated = service.updateWorkerComplaintStatus(
                id,
                status,
                authentication.getName(),
                photo,
                remarks
        );

        if (updated) {
            return ResponseEntity.ok(
                    "Complaint status updated successfully");
        }

        return ResponseEntity.badRequest().body(
                "Complaint not found or not assigned to this Worker");
    }

    // USER: View history of own complaint
    @GetMapping("/user/complaints/{id}/history")
    public ResponseEntity<List<ComplaintStatusHistoryResponse>>
            getUserComplaintHistory(
                    @PathVariable int id,
                    Authentication authentication) {

        Complaint complaint = service.getUserComplaintById(
                id, authentication.getName());

        if (complaint == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(service.getComplaintHistory(id));
    }

    // ADMIN: View history of any complaint
    @GetMapping("/admin/complaints/{id}/history")
    public ResponseEntity<List<ComplaintStatusHistoryResponse>>
            getAdminComplaintHistory(@PathVariable int id) {

        Complaint complaint = service.getComplaintById(id);

        if (complaint == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(service.getComplaintHistory(id));
    }

    // WORKER: View history of assigned complaint
    @GetMapping("/worker/complaints/{id}/history")
    public ResponseEntity<List<ComplaintStatusHistoryResponse>>
            getWorkerComplaintHistory(
                    @PathVariable int id,
                    Authentication authentication) {

        List<Complaint> complaints = service.getWorkerComplaints(
                authentication.getName());

        boolean assigned = complaints.stream()
                .anyMatch(complaint ->
                        complaint.getComplaintId() == id);

        if (!assigned) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(service.getComplaintHistory(id));
    }

    // AUTHORIZED USERS: View complaint photo
    @GetMapping("/complaints/{id}/photo")
    public ResponseEntity<Resource> getComplaintPhoto(
            @PathVariable int id,
            Authentication authentication) {

        Complaint complaint = service.getComplaintById(id);

        if (complaint == null
                || complaint.getPhotoPath() == null
                || complaint.getPhotoPath().isBlank()) {
            return ResponseEntity.notFound().build();
        }

        if (!isAuthorizedToViewComplaint(complaint, authentication)) {
            return ResponseEntity.notFound().build();
        }

        return loadPhoto(complaint.getPhotoPath());
    }

    // AUTHORIZED USERS: View worker completion photo
    @GetMapping("/complaints/{id}/completion-photo")
    public ResponseEntity<Resource> getCompletionPhoto(
            @PathVariable int id,
            Authentication authentication) {

        Complaint complaint = service.getComplaintById(id);

        if (complaint == null
                || complaint.getCompletionPhotoPath() == null
                || complaint.getCompletionPhotoPath().isBlank()) {
            return ResponseEntity.notFound().build();
        }

        if (!isAuthorizedToViewComplaint(complaint, authentication)) {
            return ResponseEntity.notFound().build();
        }

        return loadPhoto(complaint.getCompletionPhotoPath());
    }

    // Check whether the logged-in user can view this complaint
    private boolean isAuthorizedToViewComplaint(
            Complaint complaint,
            Authentication authentication) {

        if (authentication == null) {
            return false;
        }

        String email = authentication.getName();

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a ->
                        a.getAuthority().equals("ROLE_ADMIN")
                        || a.getAuthority().equals("ADMIN"));

        boolean isUser = authentication.getAuthorities().stream()
                .anyMatch(a ->
                        a.getAuthority().equals("ROLE_USER")
                        || a.getAuthority().equals("USER"));

        boolean isWorker = authentication.getAuthorities().stream()
                .anyMatch(a ->
                        a.getAuthority().equals("ROLE_WORKER")
                        || a.getAuthority().equals("WORKER"));

        if (isAdmin) {
            return true;
        }

        if (isUser) {
            return service.getUserComplaintById(
                    complaint.getComplaintId(), email) != null;
        }

        if (isWorker) {
            return service.getWorkerComplaints(email).stream()
                    .anyMatch(c ->
                            c.getComplaintId()
                                    == complaint.getComplaintId());
        }

        return false;
    }

    // Load and return an image
    private ResponseEntity<Resource> loadPhoto(String photoPath) {

        try {
            Resource image = fileStorageService.loadFile(photoPath);

            MediaType contentType = MediaTypeFactory
                    .getMediaType(image)
                    .orElse(MediaType.APPLICATION_OCTET_STREAM);

            return ResponseEntity.ok()
                    .contentType(contentType)
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "inline")
                    .body(image);

        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
