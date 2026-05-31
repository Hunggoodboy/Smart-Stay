package vn.edu.ptit.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.ptit.entity.RentPayments;
import vn.edu.ptit.entity.UtilityBills;
import vn.edu.ptit.repository.RentPaymentsRepository;
import vn.edu.ptit.repository.UtilityBillsRepository;
import vn.payos.model.webhooks.WebhookData;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PayOsPaymentService {

    private final RentPaymentsRepository rentPaymentsRepository;
    private final UtilityBillsRepository utilityBillsRepository;

    @Transactional
    public void markPaidFromWebhook(WebhookData webhookData) {
        Long orderCode = webhookData.getOrderCode();
        Long amount = webhookData.getAmount();
        String reference = webhookData.getReference();
        String code = webhookData.getCode();
        String description = webhookData.getDescription();

        if (orderCode == null) {
            throw new RuntimeException("Webhook PayOS khong co orderCode");
        }
        if (code != null && !"00".equals(code)) {
            throw new RuntimeException("Webhook PayOS chua thanh cong");
        }

        RentPayments payment = rentPaymentsRepository.findById(orderCode)
                .orElseThrow(() -> new RuntimeException("Khong tim thay ky thanh toan PayOS: " + orderCode));

        if (payment.getStatus() == RentPayments.Status.PAID) {
            return;
        }

        long expectedAmount = Math.round(payment.getTotalAmount() != null ? payment.getTotalAmount() : 0.0);
        if (amount == null || amount != expectedAmount) {
            throw new RuntimeException("So tien PayOS khong khop ky thanh toan");
        }

        payment.setStatus(RentPayments.Status.PAID);
        payment.setPaymentMethod("PAYOS");
        if (reference != null && !reference.isBlank()) {
            payment.setTransactionId(reference);
        }
        if (description != null && !description.isBlank()) {
            payment.setNotes(description);
        }
        payment.setPaidDate(LocalDate.now());
        payment.setUpdatedAt(LocalDateTime.now());

        UtilityBills utilityBill = payment.getUtilityBill();
        if (utilityBill != null) {
            utilityBill.setStatus(UtilityBills.Status.PAID);
            utilityBill.setPaidDate(payment.getPaidDate());
            utilityBill.setUpdatedAt(LocalDateTime.now());
            utilityBillsRepository.save(utilityBill);
        }

        rentPaymentsRepository.save(payment);
    }
}
