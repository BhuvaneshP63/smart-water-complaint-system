
package com.swcms.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.swcms.model.Assignment;

@Repository
public interface AssignmentRepository
        extends JpaRepository<Assignment, Integer> {

    // Fetch assigned complaints along with complaint details
    @Query("SELECT a FROM Assignment a " +
           "JOIN FETCH a.complaint " +
           "WHERE a.worker.email = :email " +
           "ORDER BY a.assignedAt DESC")
    List<Assignment> findAssignmentsByWorkerEmail(
            @Param("email") String email);

    Optional<Assignment> findByComplaintComplaintId(int complaintId);

    boolean existsByComplaintComplaintIdAndWorkerEmail(
            int complaintId, String workerEmail);

    List<Assignment> findByWorkerUserId(Integer workerId);

    void deleteByComplaintComplaintId(int complaintId);
}
