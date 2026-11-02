
package com.swcms.controller;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.swcms.dto.WorkerFeedbackRequest;
import com.swcms.dto.WorkerFeedbackResponse;
import com.swcms.service.WorkerFeedbackService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class WorkerFeedbackController {

    private final WorkerFeedbackService service;

    public WorkerFeedbackController(WorkerFeedbackService service) {
        this.service = service;
    }

    // Worker submits feedback after resolving complaint
    @PostMapping(
        value = "/worker/complaints/{id}/feedback",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<WorkerFeedbackResponse> submitFeedback(
            @PathVariable Integer id,
            @Valid @ModelAttribute WorkerFeedbackRequest request,
            Authentication authentication) {

        WorkerFeedbackResponse response = service.submitFeedback(
                id,
                request,
                authentication.getName()
        );

        return ResponseEntity.ok(response);
    }

    // Worker checks their submitted feedback
    @GetMapping("/worker/complaints/{id}/feedback")
    public ResponseEntity<WorkerFeedbackResponse> getWorkerFeedback(
            @PathVariable Integer id,
            Authentication authentication) {

        try {
            WorkerFeedbackResponse response = service.getWorkerFeedback(
                    id, authentication.getName());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            if ("Worker feedback not submitted yet".equals(e.getMessage())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            throw e;
        }
    }

    // Citizen views worker feedback
    @GetMapping("/user/complaints/{id}/worker-feedback")
    public ResponseEntity<WorkerFeedbackResponse> getCitizenWorkerFeedback(
            @PathVariable Integer id,
            Authentication authentication) {

        try {
            return ResponseEntity.ok(service.getCitizenWorkerFeedback(
                    id, authentication.getName()));
        } catch (IllegalArgumentException e) {
            if ("Worker feedback not found".equals(e.getMessage())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            throw e;
        }
    }

    // Citizen views worker feedback photo
    @GetMapping("/user/complaints/{id}/worker-feedback/photo")
    public ResponseEntity<Resource> getWorkerFeedbackPhoto(
            @PathVariable Integer id,
            Authentication authentication) {

        Resource photo = service.getCitizenWorkerFeedbackPhoto(
                id,
                authentication.getName()
        );

        MediaType mediaType = MediaTypeFactory.getMediaType(photo)
                .orElse(MediaType.APPLICATION_OCTET_STREAM);

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(
                    HttpHeaders.CONTENT_DISPOSITION,
                    "inline; filename=\"" + photo.getFilename() + "\""
                )
                .body(photo);
    }

    @GetMapping("/admin/worker-feedback")
    public ResponseEntity<java.util.List<WorkerFeedbackResponse>> getAllWorkerFeedback() {
        return ResponseEntity.ok(service.getAllWorkerFeedback());
    }

    @GetMapping("/worker-feedback/{id}/photo")
    public ResponseEntity<Resource> getFeedbackPhoto(
            @PathVariable Integer id, Authentication authentication) {
        Resource photo = service.getFeedbackPhoto(id, authentication.getName(),
                authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")),
                authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_WORKER")));
        MediaType mediaType = MediaTypeFactory.getMediaType(photo)
                .orElse(MediaType.APPLICATION_OCTET_STREAM);
        return ResponseEntity.ok().contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + photo.getFilename() + "\"")
                .body(photo);
    }
}
