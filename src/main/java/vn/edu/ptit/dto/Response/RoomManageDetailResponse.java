package vn.edu.ptit.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RoomManageDetailResponse {
    // Định danh phòng[cite: 1]
    private Long id;
    private String roomNumber;
    private String roomType;
    private String description;

    // Vị trí địa lý[cite: 1]
    private String address;
    private String ward;
    private String district;
    private String city;

    // Diện tích và quy mô[cite: 1]
    private Double areaM2;
    private Long maxOccupants;

    // Các khoản chi phí cố định và dịch vụ[cite: 1]
    private Double rentPrice;
    private Double electricityPricePerKwh;
    private Double waterPricePerM3;
    private Double internetFee;
    private Double parkingFee;
    private Double cleaningFee;

    // Trạng thái hoạt động[cite: 1]
    private String status;

    // Thông tin người mua/người thuê (liên kết với User/Buyer)
    private Long userId;
    private String userName;
    private String userEmail;
    private String userPhone;

    // Trích xuất ID hợp đồng hiện tại (nếu có liên kết)[cite: 1]
    private Long contractId;

    // Thời gian hệ thống ghi nhận[cite: 1]
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}