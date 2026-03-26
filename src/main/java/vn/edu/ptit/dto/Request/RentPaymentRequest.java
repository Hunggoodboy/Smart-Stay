package vn.edu.ptit.dto.Request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.ptit.entity.Contracts;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RentPaymentRequest {
    private Long contractId;
    private Long customerId;
    private Long roomId;
    private String billingMonth;

    private Long utilityBillId;   // nullable - gộp hóa đơn điện nước vào
    private LocalDate dueDate;
    private String notes;
}
