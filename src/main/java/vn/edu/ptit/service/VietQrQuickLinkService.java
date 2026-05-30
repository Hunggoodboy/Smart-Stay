package vn.edu.ptit.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import vn.edu.ptit.config.VietQrQuickLinkProperties;
import vn.edu.ptit.config.PayOsProperties;
import vn.edu.ptit.dto.Response.VietQrQuickLinkResponse;
import vn.edu.ptit.entity.Contracts;
import vn.edu.ptit.entity.RentPayments;
import vn.edu.ptit.entity.Rooms;
import vn.edu.ptit.entity.UtilityBills;
import vn.edu.ptit.repository.UtilityBillsRepository;
import vn.edu.ptit.service.Authentication.AuthService;
import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.v2.paymentRequests.PaymentLink;
import vn.payos.model.v2.paymentRequests.PaymentLinkStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VietQrQuickLinkService {

    private final AuthService authService;
    private final UtilityBillsRepository utilityBillsRepository;
    private final VietQrQuickLinkProperties properties;
    private final PayOsProperties payOsProperties;
    private final PayOS payOS;

    @Transactional
    public VietQrQuickLinkResponse buildQuickLinkForCurrentUser() {
        List<UtilityBills> bills = utilityBillsRepository.findNewestByCustomerId(
                authService.getCurrentUserId(),
                PageRequest.of(0, 1));
        UtilityBills bill = bills.stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Hien tai ban chua co hoa don"));

        double amount = resolveTotalAmount(bill);
        String addInfo = buildAddInfo(bill);
        RentPayments rentPayment = bill.getRentPayment();
        if (rentPayment == null || rentPayment.getId() == null) {
            throw new RuntimeException("Hoa don chua co ky thanh toan");
        }

        CreatePaymentLinkResponse paymentLink = createPayOsPaymentLink(rentPayment, amount, addInfo);
        if (paymentLink.getStatus() == PaymentLinkStatus.PAID) {
            markPaymentPaid(rentPayment, bill);
        }
        String accountName = properties.getAccountName() != null ? properties.getAccountName() : "";

        String masked = maskAccountNo(properties.getAccountNo());

        return VietQrQuickLinkResponse.builder()
                .imageUrl(null)
                .amount(amount)
                .addInfo(addInfo)
                .accountName(StringUtils.hasText(paymentLink.getAccountName()) ? paymentLink.getAccountName() : accountName)
                .bankId(StringUtils.hasText(paymentLink.getBin()) ? paymentLink.getBin() : properties.getBankId())
                .accountNoMasked(masked)
                .checkoutUrl(paymentLink.getCheckoutUrl())
                .qrCode(paymentLink.getQrCode())
                .orderCode(paymentLink.getOrderCode())
                .paymentLinkId(paymentLink.getPaymentLinkId())
                .status(paymentLink.getStatus() != null ? paymentLink.getStatus().name() : null)
                .build();
    }

    private CreatePaymentLinkResponse createPayOsPaymentLink(RentPayments rentPayment, double amount, String addInfo) {
        if (!StringUtils.hasText(payOsProperties.getReturnUrl())
                || !StringUtils.hasText(payOsProperties.getCancelUrl())) {
            throw new RuntimeException("Chua cau hinh PayOS return/cancel URL");
        }

        CreatePaymentLinkRequest request = CreatePaymentLinkRequest.builder()
                .orderCode(rentPayment.getId())
                .amount((long) Math.round(amount))
                .description(buildPayOsDescription(rentPayment, addInfo))
                .returnUrl(payOsProperties.getReturnUrl())
                .cancelUrl(payOsProperties.getCancelUrl())
                .build();

        try {
            return payOS.paymentRequests().create(request);
        } catch (Exception ex) {
            return getReusablePayOsPaymentLink(rentPayment.getId(), amount, request.getDescription(), ex);
        }
    }

    private CreatePaymentLinkResponse getReusablePayOsPaymentLink(
            Long orderCode,
            double amount,
            String description,
            Exception createException) {
        try {
            PaymentLink paymentLink = payOS.paymentRequests().get(orderCode);
            if (paymentLink.getStatus() == PaymentLinkStatus.PENDING
                    || paymentLink.getStatus() == PaymentLinkStatus.PROCESSING
                    || paymentLink.getStatus() == PaymentLinkStatus.UNDERPAID
                    || paymentLink.getStatus() == PaymentLinkStatus.PAID) {
                return CreatePaymentLinkResponse.builder()
                        .bin("")
                        .accountNumber("")
                        .accountName("")
                        .amount((long) Math.round(amount))
                        .description(description)
                        .orderCode(orderCode)
                        .currency("VND")
                        .paymentLinkId(paymentLink.getId())
                        .status(paymentLink.getStatus())
                        .checkoutUrl(paymentLink.getStatus() == PaymentLinkStatus.PAID
                                ? ""
                                : "https://pay.payos.vn/web/" + paymentLink.getId())
                        .qrCode("")
                        .build();
            }
            throw new RuntimeException("Link PayOS hien tai co trang thai " + paymentLink.getStatus());
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception getException) {
            throw new RuntimeException("Khong tao duoc link thanh toan PayOS: " + createException.getMessage());
        }
    }

    private double resolveTotalAmount(UtilityBills bill) {
        RentPayments rentPayment = bill.getRentPayment();
        if (rentPayment != null && rentPayment.getTotalAmount() != null) {
            return rentPayment.getTotalAmount();
        }

        double utilityAmount = bill.getTotalAmount() != null ? bill.getTotalAmount() : 0.0;
        double rentAmount = 0.0;

        Contracts contract = bill.getContract();
        if (contract != null && contract.getMonthlyRent() != null) {
            rentAmount = contract.getMonthlyRent();
        } else {
            Rooms room = bill.getRoom();
            if (room != null && room.getRentPrice() != null) {
                rentAmount = room.getRentPrice();
            }
        }

        return utilityAmount + rentAmount;
    }

    private String buildAddInfo(UtilityBills bill) {
        String month = bill.getBillingMonth() != null ? bill.getBillingMonth() : "";
        return "Thanh toan thang " + month;
    }

    private void markPaymentPaid(RentPayments rentPayment, UtilityBills bill) {
        if (rentPayment.getStatus() != RentPayments.Status.PAID) {
            rentPayment.setStatus(RentPayments.Status.PAID);
            rentPayment.setPaymentMethod("PAYOS");
            rentPayment.setPaidDate(LocalDate.now());
            rentPayment.setUpdatedAt(LocalDateTime.now());
        }
        if (bill != null && bill.getStatus() != UtilityBills.Status.PAID) {
            bill.setStatus(UtilityBills.Status.PAID);
            bill.setPaidDate(rentPayment.getPaidDate());
            bill.setUpdatedAt(LocalDateTime.now());
            utilityBillsRepository.save(bill);
        }
    }

    private String buildPayOsDescription(RentPayments rentPayment, String addInfo) {
        String description = "SS" + rentPayment.getId();
        if (StringUtils.hasText(addInfo) && addInfo.length() <= 25) {
            description = addInfo;
        }
        return description.length() > 25 ? description.substring(0, 25) : description;
    }

    private String maskAccountNo(String accountNo) {
        if (!StringUtils.hasText(accountNo) || accountNo.length() <= 4) {
            return accountNo;
        }
        String suffix = accountNo.substring(accountNo.length() - 4);
        return "****" + suffix;
    }
}
