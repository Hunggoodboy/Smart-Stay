package vn.edu.ptit.dto.Request;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateRoomManageRequest {
    // Thông tin định danh phòng
    private String roomNumber;
    private String roomType;
    private String description;

    // Hợp đồng
    private String contractCode;

    // Thông tin địa chỉ[cite: 7]
    private String address;
    private String ward;
    private String district;
    private String city;

    // Thông tin diện tích và quy mô[cite: 7]
    private Double areaM2;
    private Long maxOccupants;

    // Thông tin tài chính[cite: 7]
    private Double rentPrice;
    private Double electricityPricePerKwh;
    private Double waterPricePerM3;
    private Double internetFee;
    private Double parkingFee;
    private Double cleaningFee;

    // Trạng thái ban đầu (AVAILABLE, RENTED, MAINTENANCE)[cite: 7]
    private String status;

    /**
     * Dùng Email để định danh khách thuê khi tạo phòng gán trực tiếp.
     * Nếu phòng trống thì để null.[cite: 4]
     */
    private String customerEmail;


}