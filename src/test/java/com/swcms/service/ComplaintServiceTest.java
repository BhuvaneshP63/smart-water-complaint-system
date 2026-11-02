package com.swcms.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.swcms.model.Complaint;
import com.swcms.repository.ComplaintRepository;
import com.swcms.repository.UserRepository;
import com.swcms.repository.AssignmentRepository;
import com.swcms.repository.ComplaintStatusHistoryRepository;
import com.swcms.model.User;
import com.swcms.dto.ComplaintRequest;

@ExtendWith(MockitoExtension.class)
class ComplaintServiceTest {

    @Mock
    private ComplaintRepository repository;

    @Mock private UserRepository users;
    @Mock private AssignmentRepository assignments;
    @Mock private ComplaintStatusHistoryRepository histories;
    @Mock private FileStorageService fileStorage;

    @InjectMocks
    private ComplaintService service;

    private Complaint complaint;

    @BeforeEach
    void setUp() {
        complaint = new Complaint(
            101,
            "Bhuvanesh",
            "Coimbatore",
            "Water Leakage",
            "Pipeline leakage"
        );
    }

    @Test
    @DisplayName("Add a new complaint")
    void testAddComplaint() {
        User user = new User(); user.setName("Bhuvanesh"); user.setEmail("citizen@example.com"); user.setRole("USER");
        when(users.findByEmail("citizen@example.com")).thenReturn(Optional.of(user));
        when(repository.save(any(Complaint.class))).thenAnswer(invocation -> invocation.getArgument(0));
        ComplaintRequest request = new ComplaintRequest();
        request.setCustomerName("Bhuvanesh"); request.setAddress("Coimbatore");
        request.setComplaintType("Water Leakage"); request.setDescription("Pipeline leakage");
        Complaint result = service.addComplaint(request, "citizen@example.com");

        assertNotNull(result);
        assertEquals("Bhuvanesh", result.getCustomerName());
        verify(repository).save(any(Complaint.class));
    }

    @Test
    @DisplayName("Get all complaints")
    void testGetAllComplaints() {
        when(repository.findAllByOrderByComplaintIdDesc()).thenReturn(List.of(complaint));

        List<Complaint> result = service.getAllComplaints();

        assertEquals(1, result.size());
        verify(repository).findAllByOrderByComplaintIdDesc();
    }

    @Test
    @DisplayName("Get complaint by ID")
    void testGetComplaintById() {
        when(repository.findById(101)).thenReturn(Optional.of(complaint));

        Complaint result = service.getComplaintById(101);

        assertNotNull(result);
        assertEquals(101, result.getComplaintId());
    }

    @Test
    @DisplayName("Update complaint status")
    void testUpdateStatus() {
        complaint.setStatus("In Progress");
        when(repository.findById(101)).thenReturn(Optional.of(complaint));

        boolean result = service.updateStatus(101, "Resolved");

        assertTrue(result);
        assertEquals("Resolved", complaint.getStatus());
        verify(repository).save(complaint);
    }

    @Test
    @DisplayName("Delete complaint")
    void testDeleteComplaint() {
        when(repository.existsById(101)).thenReturn(true);

        boolean result = service.deleteComplaint(101);

        assertTrue(result);
        verify(repository).deleteById(101);
    }

    @Test
    @DisplayName("Complaint not found")
    void testComplaintNotFound() {
        when(repository.findById(999)).thenReturn(Optional.empty());

        Complaint result = service.getComplaintById(999);

        assertNull(result);
    }
}
