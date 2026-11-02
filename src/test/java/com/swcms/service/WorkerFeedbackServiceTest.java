package com.swcms.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.swcms.dto.WorkerFeedbackRequest;
import com.swcms.model.Complaint;
import com.swcms.model.User;
import com.swcms.model.WorkerFeedback;
import com.swcms.repository.*;

@ExtendWith(MockitoExtension.class)
class WorkerFeedbackServiceTest {
    @Mock WorkerFeedbackRepository feedbacks;
    @Mock ComplaintRepository complaints;
    @Mock AssignmentRepository assignments;
    @Mock UserRepository users;
    @Mock FileStorageService files;
    @InjectMocks WorkerFeedbackService service;

    @Test
    void submitsRatingAndCommentOnceForResolvedAssignedComplaint() {
        Complaint complaint=new Complaint(101,"Citizen","River Rd","Leak","Pipe leak");
        complaint.setStatus("Resolved");
        User worker=new User(); worker.setName("Worker One"); worker.setEmail("worker@example.com"); worker.setRole("WORKER");
        when(assignments.existsByComplaintComplaintIdAndWorkerEmail(101,"worker@example.com")).thenReturn(true);
        when(complaints.findById(101)).thenReturn(Optional.of(complaint));
        when(feedbacks.existsByComplaintComplaintId(101)).thenReturn(false,true);
        when(users.findByEmail("worker@example.com")).thenReturn(Optional.of(worker));
        when(feedbacks.save(any(WorkerFeedback.class))).thenAnswer(call -> call.getArgument(0));
        WorkerFeedbackRequest request=new WorkerFeedbackRequest(); request.setRating(4); request.setComment("Repair completed");

        var saved=service.submitFeedback(101,request,"worker@example.com");

        assertEquals(4,saved.getRating());
        assertEquals("Repair completed",saved.getComment());
        assertEquals("Worker One",saved.getWorkerName());
        assertThrows(IllegalArgumentException.class, () -> service.submitFeedback(101,request,"worker@example.com"));
        verify(feedbacks,times(1)).save(any(WorkerFeedback.class));
    }
}
