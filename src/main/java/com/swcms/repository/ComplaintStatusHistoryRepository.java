
package com.swcms.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.swcms.model.ComplaintStatusHistory;

public interface ComplaintStatusHistoryRepository
        extends JpaRepository<ComplaintStatusHistory, Integer> {

    List<ComplaintStatusHistory> findTop20ByOrderByUpdatedAtDesc();

    List<ComplaintStatusHistory>
        findByComplaintComplaintIdOrderByUpdatedAtAsc(int complaintId);
}
