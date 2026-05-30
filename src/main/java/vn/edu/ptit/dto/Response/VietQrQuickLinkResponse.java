package vn.edu.ptit.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VietQrQuickLinkResponse {
    private String imageUrl;
    private Double amount;
    private String addInfo;
    private String accountName;
    private String bankId;
    private String accountNoMasked;
    private String checkoutUrl;
    private String qrCode;
    private Long orderCode;
    private String paymentLinkId;
    private String status;
}
