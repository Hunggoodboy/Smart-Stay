package vn.edu.ptit.dto.Request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmRentPaymentRequest {
    private LocalDate paidDate;
    private String paymentMethod;
    private String transactionId;
    private String receiptUrl;
    private String notes;
}
