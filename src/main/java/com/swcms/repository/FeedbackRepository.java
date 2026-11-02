package com.swcms.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.swcms.model.Feedback;
import org.springframework.core.io.Resource;
@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Integer> {

    boolean existsByComplaintComplaintId(Integer complaintId);

    @Query("SELECT f FROM Feedback f " +
           "JOIN FETCH f.complaint c " +
           "JOIN FETCH c.user " +
           "WHERE c.user.email = :email " +
           "ORDER BY f.submittedAt DESC")
    List<Feedback> findByCitizenEmail(@Param("email") String email);

    @Query("SELECT f FROM Feedback f " +
           "JOIN FETCH f.complaint c " +
           "JOIN FETCH c.user " +
           "ORDER BY f.submittedAt DESC")
    List<Feedback> findAllWithComplaintAndUser();
}