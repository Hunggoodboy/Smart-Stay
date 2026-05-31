package vn.edu.ptit.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.ptit.dto.Request.ConfirmRentPaymentRequest;
import vn.edu.ptit.dto.Response.ApiResponse;
import vn.edu.ptit.dto.Response.RentPaymentResponse;
import vn.edu.ptit.entity.RentPayments;
import vn.edu.ptit.repository.LandlordRentPaymentRepository;
import vn.edu.ptit.repository.UtilityBillsRepository;
import vn.edu.ptit.service.Authentication.AuthService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class LandlordRentPaymentService {
    private final AuthService authService;
    private final LandlordRentPaymentRepository landlordRentPaymentRepository;
    private final UtilityBillsRepository utilityBillsRepository;

    @Transactional(readOnly = true)
    public List<RentPaymentResponse> getPaymentsForCurrentLandlord(RentPayments.Status status) {
        Long landlordId = authService.getCurrentLandLord().getId();
        List<RentPayments> payments = landlordRentPaymentRepository.findAllForLandlord(
                landlordId,
                status != null ? status.name() : null);

        List<RentPaymentResponse> responses = new ArrayList<>();
        for (RentPayments payment : payments) {
            RentPaymentResponse response = RentPaymentResponse.fromEntity(payment);
            responses.add(response);
        }

        return responses;
    }

    @Transactional(readOnly = true)
    public List<RentPaymentResponse> getUnpaidPaymentsForCurrentLandlord() {
        Long landlordId = authService.getCurrentLandLord().getId();
        List<RentPayments> payments = landlordRentPaymentRepository.findUnpaidForLandlord(
                landlordId,
                RentPayments.Status.PAID.name());

        List<RentPaymentResponse> responses = new ArrayList<>();
        for (RentPayments payment : payments) {
            RentPaymentResponse response = RentPaymentResponse.fromEntity(payment);
            responses.add(response);
        }

        return responses;
    }

    @Transactional
    public ApiResponse markAsPaid(Long paymentId, ConfirmRentPaymentRequest request) {
        Long landlordId = authService.getCurrentLandLord().getId();
        RentPayments payment = landlordRentPaymentRepository.findForLandlordById(paymentId, landlordId)
                .orElseThrow(() -> new RuntimeException("Khong tim thay ky thanh toan thuoc chu nha hien tai"));

        if (payment.getStatus() == RentPayments.Status.PAID) {
            return ApiResponse.builder()
                    .success(true)
                    .message("Ky thanh toan nay da duoc danh dau PAID")
                    .build();
        }

        payment.setStatus(RentPayments.Status.PAID);

        LocalDate paidDate = LocalDate.now();
        if (request != null && request.getPaidDate() != null) {
            paidDate = request.getPaidDate();
        }

        payment.setPaidDate(paidDate);
        payment.setUpdatedAt(LocalDateTime.now());

        if (payment.getUtilityBill() != null) {
            payment.getUtilityBill().setStatus(vn.edu.ptit.entity.UtilityBills.Status.PAID);
            payment.getUtilityBill().setPaidDate(paidDate);
            payment.getUtilityBill().setUpdatedAt(LocalDateTime.now());
            utilityBillsRepository.save(payment.getUtilityBill());
        }

        if (request != null) {
            String paymentMethod = request.getPaymentMethod();
            String transactionId = request.getTransactionId();
            String receiptUrl = request.getReceiptUrl();
            String notes = request.getNotes();

            if (paymentMethod != null && !paymentMethod.isBlank()) {
                payment.setPaymentMethod(request.getPaymentMethod());
            }
            if (transactionId != null && !transactionId.isBlank()) {
                payment.setTransactionId(transactionId);
            }
            if (receiptUrl != null && !receiptUrl.isBlank()) {
                payment.setReceiptUrl(receiptUrl);
            }
            if (notes != null && !notes.isBlank()) {
                payment.setNotes(notes);
            }
        }

        landlordRentPaymentRepository.save(payment);
        return ApiResponse.builder()
                .success(true)
                .message("Da cap nhat trang thai thanh toan thanh PAID")
                .build();
    }
}
