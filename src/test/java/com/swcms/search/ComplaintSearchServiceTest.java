package com.swcms.search;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.swcms.model.Complaint;

class ComplaintSearchServiceTest {

    private ComplaintSearchService service;
    private List<Complaint> complaints;

    @BeforeEach
    void setUp() {

        service = new ComplaintSearchService();

        complaints = new ArrayList<>();

        complaints.add(new Complaint(
                201,
                "Bhuvanesh",
                "Coimbatore",
                "Water Leakage",
                "Pipeline leakage"
        ));

        complaints.add(new Complaint(
                202,
                "Bhuvanesh",
                "Coimbatore",
                "Low Pressure",
                "Low water pressure"
        ));

        complaints.add(new Complaint(
                203,
                "Arun",
                "Pollachi",
                "Water Leakage",
                "Road pipeline leakage"
        ));
    }

    @Test
    @DisplayName("Search complaints by customer name")
    void testSearchByCustomerName() {

        List<Complaint> result =
                service.searchByCustomerName(complaints, "Bhuvanesh");

        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Search complaints by complaint type")
    void testSearchByComplaintType() {

        List<Complaint> result =
                service.searchByComplaintType(
                        complaints,
                        "Water Leakage"
                );

        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Search for customer with no complaints")
    void testCustomerNotFound() {

        List<Complaint> result =
                service.searchByCustomerName(
                        complaints,
                        "Kumar"
                );

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Empty customer name should throw exception")
    void testEmptyCustomerName() {

        assertThrows(
                IllegalArgumentException.class,
                () -> service.searchByCustomerName(
                        complaints,
                        ""
                )
        );
    }

    @Test
    @DisplayName("Null complaint list should throw exception")
    void testNullComplaintList() {

        assertThrows(
                IllegalArgumentException.class,
                () -> service.searchByCustomerName(
                        null,
                        "Bhuvanesh"
                )
        );
    }
}