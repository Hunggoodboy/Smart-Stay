package vn.edu.ptit.dto.Request;

import lombok.Data;
import vn.edu.ptit.entity.RentalRequests;

@Data
public class UpdateRentalRequestStatusRequest {
    private RentalRequests.Status status;
}
