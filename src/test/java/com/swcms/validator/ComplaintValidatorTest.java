package com.swcms.validator;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.swcms.model.Complaint;

class ComplaintValidatorTest {

    private ComplaintValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ComplaintValidator();
    }

    @Test
    @DisplayName("Valid complaint should pass validation")
    void testValidComplaint() {

        Complaint complaint = new Complaint(
                201,
                "Bhuvanesh",
                "Coimbatore",
                "Water Leakage",
                "Pipeline leakage"
        );

        assertTrue(validator.isValid(complaint));
    }

    @Test
    @DisplayName("Null complaint should fail validation")
    void testNullComplaint() {

        assertFalse(validator.isValid(null));
    }

    @Test
    @DisplayName("Invalid complaint ID should fail validation")
    void testInvalidComplaintId() {

        Complaint complaint = new Complaint(
                -1,
                "Bhuvanesh",
                "Coimbatore",
                "Water Leakage",
                "Pipeline leakage"
        );

        assertFalse(validator.isValid(complaint));
    }

    @Test
    @DisplayName("Empty description should fail validation")
    void testEmptyDescription() {

        Complaint complaint = new Complaint(
                202,
                "Bhuvanesh",
                "Coimbatore",
                "Water Leakage",
                ""
        );

        assertFalse(validator.isValid(complaint));
    }
}