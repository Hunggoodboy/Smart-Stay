package vn.edu.ptit.service.payment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.ptit.entity.RentPayments;
import vn.edu.ptit.entity.UtilityBills;
import vn.edu.ptit.repository.RentPaymentsRepository;
import vn.edu.ptit.repository.UtilityBillsRepository;
import vn.edu.ptit.Exception.PaymentException;
import vn.edu.ptit.Exception.ResourceNotFoundException;
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
            throw new PaymentException("Webhook PayOS khong co orderCode");
        }
        if (code != null && !"00".equals(code)) {
            throw new PaymentException("Webhook PayOS chua thanh cong");
        }

        Long rentPaymentId = resolveRentPaymentId(orderCode);
        RentPayments payment = rentPaymentsRepository.findById(rentPaymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Ky thanh toan PayOS", orderCode));

        if (payment.getStatus() == RentPayments.Status.PAID) {
            return;
        }

        long expectedAmount = Math.round(payment.getTotalAmount() != null ? payment.getTotalAmount() : 0.0);
        if (amount == null || amount != expectedAmount) {
            throw new PaymentException("So tien PayOS khong khop ky thanh toan");
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

    private Long resolveRentPaymentId(Long orderCode) {
        if (orderCode >= VietQrQuickLinkService.PAYOS_ORDER_CODE_FACTOR) {
            return orderCode / VietQrQuickLinkService.PAYOS_ORDER_CODE_FACTOR;
        }
        return orderCode;
    }
}
