package com.swcms.validator;

import com.swcms.model.Complaint;

public class ComplaintValidator {

    public boolean isValid(Complaint complaint) {

        if (complaint == null) {
            return false;
        }

        if (complaint.getComplaintId() <= 0) {
            return false;
        }

        if (complaint.getCustomerName() == null ||
            complaint.getCustomerName().isBlank()) {
            return false;
        }

        if (complaint.getAddress() == null ||
            complaint.getAddress().isBlank()) {
            return false;
        }

        if (complaint.getComplaintType() == null ||
            complaint.getComplaintType().isBlank()) {
            return false;
        }

        if (complaint.getDescription() == null ||
            complaint.getDescription().isBlank()) {
            return false;
        }

        return true;
    }
}