package vn.edu.ptit.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import vn.edu.ptit.config.VietQrQuickLinkProperties;
import vn.edu.ptit.dto.Response.VietQrQuickLinkResponse;
import vn.edu.ptit.entity.Contracts;
import vn.edu.ptit.entity.RentPayments;
import vn.edu.ptit.entity.Rooms;
import vn.edu.ptit.entity.UtilityBills;
import vn.edu.ptit.repository.UtilityBillsRepository;
import vn.edu.ptit.service.Authentication.AuthService;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VietQrQuickLinkService {

    private final AuthService authService;
    private final UtilityBillsRepository utilityBillsRepository;
    private final VietQrQuickLinkProperties properties;

    @Transactional(readOnly = true)
    public VietQrQuickLinkResponse buildQuickLinkForCurrentUser() {
        if (!StringUtils.hasText(properties.getBankId()) || !StringUtils.hasText(properties.getAccountNo())) {
            throw new RuntimeException("Chua cau hinh VietQR quick link");
        }

        List<UtilityBills> bills = utilityBillsRepository.findNewestByCustomerId(
                authService.getCurrentUserId(),
                PageRequest.of(0, 1));
        UtilityBills bill = bills.stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Hien tai ban chua co hoa don"));

        double amount = resolveTotalAmount(bill);
        String addInfo = buildAddInfo(bill);
        String accountName = properties.getAccountName() != null ? properties.getAccountName() : "";

        String template = StringUtils.hasText(properties.getTemplate()) ? properties.getTemplate() : "compact2";
        String base = "https://img.vietqr.io/image/" + properties.getBankId()
                + "-" + properties.getAccountNo()
                + "-" + template + ".png";

        String query = "?amount=" + Math.round(amount)
                + "&addInfo=" + urlEncode(addInfo)
                + "&accountName=" + urlEncode(accountName);

        String masked = maskAccountNo(properties.getAccountNo());

        return VietQrQuickLinkResponse.builder()
                .imageUrl(base + query)
                .amount(amount)
                .addInfo(addInfo)
                .accountName(accountName)
                .bankId(properties.getBankId())
                .accountNoMasked(masked)
                .build();
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

    private String urlEncode(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String maskAccountNo(String accountNo) {
        if (!StringUtils.hasText(accountNo) || accountNo.length() <= 4) {
            return accountNo;
        }
        String suffix = accountNo.substring(accountNo.length() - 4);
        return "****" + suffix;
    }
}
