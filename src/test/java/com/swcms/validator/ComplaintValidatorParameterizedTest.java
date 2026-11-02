package com.swcms.validator;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.swcms.model.Complaint;

class ComplaintValidatorParameterizedTest {

    private ComplaintValidator validator = new ComplaintValidator();

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -10, -100})
    void testInvalidComplaintIds(int id) {

        Complaint complaint = new Complaint(
                id,
                "Bhuvanesh",
                "Coimbatore",
                "Water Leakage",
                "Pipeline leakage"
        );

        assertFalse(validator.isValid(complaint));
    }
}