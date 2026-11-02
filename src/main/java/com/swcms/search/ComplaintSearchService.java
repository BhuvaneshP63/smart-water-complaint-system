package com.swcms.search;

import java.util.ArrayList;
import java.util.List;

import com.swcms.model.Complaint;

public class ComplaintSearchService {

    public List<Complaint> searchByCustomerName(
            List<Complaint> complaints, String customerName) {

        if (complaints == null) {
            throw new IllegalArgumentException("Complaint list cannot be null");
        }

        if (customerName == null || customerName.isBlank()) {
            throw new IllegalArgumentException("Customer name cannot be empty");
        }

        List<Complaint> result = new ArrayList<>();

        for (Complaint complaint : complaints) {
            if (complaint.getCustomerName()
                    .equalsIgnoreCase(customerName)) {
                result.add(complaint);
            }
        }

        return result;
    }

    public List<Complaint> searchByComplaintType(
            List<Complaint> complaints, String complaintType) {

        if (complaints == null) {
            throw new IllegalArgumentException("Complaint list cannot be null");
        }

        if (complaintType == null || complaintType.isBlank()) {
            throw new IllegalArgumentException("Complaint type cannot be empty");
        }

        List<Complaint> result = new ArrayList<>();

        for (Complaint complaint : complaints) {
            if (complaint.getComplaintType()
                    .equalsIgnoreCase(complaintType)) {
                result.add(complaint);
            }
        }

        return result;
    }
}