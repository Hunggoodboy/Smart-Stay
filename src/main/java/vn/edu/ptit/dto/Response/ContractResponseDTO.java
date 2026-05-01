package vn.edu.ptit.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ContractResponseDTO {
    private Long id;
    private String contractCode;
    private LocalDate startDate;
    private LocalDate endDate;
    private Double monthlyRent;
    private Double depositAmount;
    private Long billingDate;
    private String status;

    // Thông tin chi phí dịch vụ
    private Double electricityPricePerKwh;
    private Double waterPricePerM3;
    private Double internetFee;
    private Double parkingFee;
    private Double cleaningFee;

    // Thông tin các bên tham gia (DTO rút gọn)
    private UserResponseDTO landLord;
    private UserResponseDTO customer;

    // Thông tin phòng (Chỉ lấy địa chỉ để tránh lặp)[cite: 3]
    private String roomAddress;
}