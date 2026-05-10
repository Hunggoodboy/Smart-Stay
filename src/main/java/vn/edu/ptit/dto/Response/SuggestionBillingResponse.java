package vn.edu.ptit.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SuggestionBillingResponse {

    // ===== Các loại phí cố định =====
    private Double rentPrice;               // Tiền thuê phòng
    private Double internetFee;             // Phí internet
    private Double parkingFee;              // Phí gửi xe
    private Double cleaningFee;             // Phí vệ sinh

    // ===== Các loại phí theo mức sử dụng =====
    private Double electricityPricePerKwh;  // Giá điện trên 1 kWh
    private Double waterPricePerM3;         // Giá nước trên 1 m3

}