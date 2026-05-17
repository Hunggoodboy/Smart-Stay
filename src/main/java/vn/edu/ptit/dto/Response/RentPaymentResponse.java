package vn.edu.ptit.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.ptit.entity.Contracts;
import vn.edu.ptit.entity.RentPayments;
import vn.edu.ptit.entity.Rooms;
import vn.edu.ptit.entity.User;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RentPaymentResponse {
    private Long id;
    private String billingMonth;
    private Double rentAmount;
    private Double utilityAmount;
    private Double lateFee;
    private Double totalAmount;
    private LocalDate dueDate;
    private LocalDate paidDate;
    private String status;
    private String paymentMethod;
    private String transactionId;
    private String receiptUrl;
    private String notes;

    private Long contractId;
    private String contractCode;

    private Long roomId;
    private String roomNumber;
    private String roomAddress;

    private Long customerId;
    private String customerName;
    private String customerEmail;
    private String customerPhoneNumber;

    private Long utilityBillId;

    public static RentPaymentResponse fromEntity(RentPayments payment) {
        Contracts contract = payment.getContract();
        Rooms room = payment.getRoom();
        User customer = payment.getCustomer();

        return RentPaymentResponse.builder()
                .id(payment.getId())
                .billingMonth(payment.getBillingMonth())
                .rentAmount(payment.getRentAmount())
                .utilityAmount(payment.getUtilityAmount())
                .lateFee(payment.getLateFee())
                .totalAmount(payment.getTotalAmount())
                .dueDate(payment.getDueDate())
                .paidDate(payment.getPaidDate())
                .status(payment.getStatus() != null ? payment.getStatus().name() : null)
                .paymentMethod(payment.getPaymentMethod())
                .transactionId(payment.getTransactionId())
                .receiptUrl(payment.getReceiptUrl())
                .notes(payment.getNotes())
                .contractId(contract != null ? contract.getId() : null)
                .contractCode(contract != null ? contract.getContractCode() : null)
                .roomId(room != null ? room.getId() : null)
                .roomNumber(room != null ? room.getRoomNumber() : null)
                .roomAddress(room != null ? room.getAddress() : null)
                .customerId(customer != null ? customer.getId() : null)
                .customerName(customer != null ? customer.getFullName() : null)
                .customerEmail(customer != null ? customer.getEmail() : null)
                .customerPhoneNumber(customer != null ? customer.getPhoneNumber() : null)
                .utilityBillId(payment.getUtilityBill() != null ? payment.getUtilityBill().getId() : null)
                .build();
    }
}
