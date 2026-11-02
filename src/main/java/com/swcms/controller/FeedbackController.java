
package com.swcms.controller;

import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.swcms.dto.FeedbackRequest;
import com.swcms.dto.FeedbackResponse;
import com.swcms.service.FeedbackService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173")
public class FeedbackController {

    private final FeedbackService feedbackService;

    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    // 1. Submit citizen feedback
    @PostMapping(
            value = "/user/complaints/{id}/feedback",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> submitFeedback(
            @PathVariable Integer id,
            @Valid @ModelAttribute FeedbackRequest request,
            Authentication authentication) {

        try {
            FeedbackResponse response =
                    feedbackService.submitFeedback(
                            id,
                            request,
                            authentication.getName());

            return ResponseEntity.status(201).body(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 2. Get feedback submitted by logged-in citizen
    @GetMapping("/user/feedback")
    public ResponseEntity<List<FeedbackResponse>> getCitizenFeedback(
            Authentication authentication) {

        return ResponseEntity.ok(
                feedbackService.getCitizenFeedback(
                        authentication.getName()));
    }

    // 3. Get all feedback (Admin)
    @GetMapping("/admin/feedback")
    public ResponseEntity<List<FeedbackResponse>> getAllFeedback() {

        return ResponseEntity.ok(
                feedbackService.getAllFeedback());
    }

    // 4. Get feedback photo with role-based authorization
    @GetMapping("/feedback/{id}/photo")
    public ResponseEntity<?> getFeedbackPhoto(
            @PathVariable Integer id,
            Authentication authentication) {

        try {

            boolean isAdmin = authentication.getAuthorities()
                    .stream()
                    .anyMatch(a ->
                            a.getAuthority().equals("ROLE_ADMIN"));

            boolean isWorker = authentication.getAuthorities()
                    .stream()
                    .anyMatch(a ->
                            a.getAuthority().equals("ROLE_WORKER"));

            Resource photo = feedbackService.getFeedbackPhoto(
                    id,
                    authentication.getName(),
                    isAdmin,
                    isWorker);

            MediaType mediaType = MediaTypeFactory
                    .getMediaType(photo)
                    .orElse(MediaType.APPLICATION_OCTET_STREAM);

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .body(photo);

        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }
 // 5. Worker views citizen feedback for assigned complaints
    @GetMapping("/worker/feedback")
    public ResponseEntity<List<FeedbackResponse>> getWorkerFeedback(
            Authentication authentication) {

        return ResponseEntity.ok(
                feedbackService.getWorkerFeedback(
                        authentication.getName()));
    }
}
