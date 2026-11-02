package com.swcms.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.swcms.model.WorkerFeedback;

@Repository
public interface WorkerFeedbackRepository
        extends JpaRepository<WorkerFeedback, Integer> {

    boolean existsByComplaintComplaintId(Integer complaintId);

    Optional<WorkerFeedback> findByComplaintComplaintId(Integer complaintId);

    java.util.List<WorkerFeedback> findAllByOrderBySubmittedAtDesc();

    Optional<WorkerFeedback> findByFeedbackId(Integer feedbackId);
}
