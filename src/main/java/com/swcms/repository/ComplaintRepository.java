package com.swcms.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.swcms.model.Complaint;

@Repository
public interface ComplaintRepository
        extends JpaRepository<Complaint, Integer> {

    List<Complaint> findByUserUserId(Integer userId);

    List<Complaint> findAllByOrderByComplaintIdDesc();
    
    List<Complaint> findByUserEmail(String email);

}
