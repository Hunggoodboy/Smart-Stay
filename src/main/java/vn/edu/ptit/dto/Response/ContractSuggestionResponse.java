package vn.edu.ptit.dto.Response;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ContractSuggestionResponse {

    // ==========================================
    // 1. Thông tin Bên Cho Thuê (LandLord)
    // ==========================================
    private Long landLordId;
    private String landLordName;
    private String landLordIdentityNumber;
    private String landLordAddress;

    // ==========================================
    // 2. Thông tin Bên Thuê (customer)
    // ==========================================
    private Long customerId;
    private String customerName;
    private String customerIdentityNumber;
    private String customerAddress;

    // ==========================================
    // 3. Thông tin Phòng và Tài chính cơ bản
    // ==========================================
    private Long roomPostId;   // ID bài đăng gốc (thay thế roomId)
    private String roomAddress;
    private Double roomArea; // Đơn vị: m2
    private Double rentPrice;
    private String address;
    private String ward;
    private String district;
    private String city;
    private Double areaM2;
    private Long maxOccupants;
    private Double electricityPricePerKwh;
    private Double waterPricePerM3;
    private Double internetFee;
    private Double parkingFee;
    private Double cleaningFee;

}