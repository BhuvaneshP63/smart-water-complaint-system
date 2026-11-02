package com.swcms.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.swcms.repository.ComplaintRepository;

@ExtendWith(MockitoExtension.class)
class ComplaintServiceExceptionTest {

    @Mock
    private ComplaintRepository repository;

    @InjectMocks
    private ComplaintService service;

    @Test
    void testNullComplaint() {
        assertThrows(
            IllegalArgumentException.class,
            () -> service.addComplaint(null, "citizen@example.com")
        );
    }
}
