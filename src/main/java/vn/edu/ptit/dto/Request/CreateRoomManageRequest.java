package vn.edu.ptit.dto.Request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateRoomManageRequest {
    // Thông tin định danh phòng
    private String roomNumber;
    private String roomType;
    private String description;

    /**
     * ID của hợp đồng đã tạo trước đó.
     * Phòng quản lý được tạo SAU hợp đồng → dùng contractId (không dùng contractCode nữa).
     */
    private Long contractId;

    /**
     * ID bài đăng gốc (RoomPost) tạo ra phòng này — dùng để check phòng đã có người thuê.
     * Optional: truyền vào nếu phòng được tạo từ luồng RentalRequest.
     */
    private Long roomPostId;

    // Thông tin địa chỉ
    private String address;
    private String ward;
    private String district;
    private String city;

    // Thông tin diện tích và quy mô
    private Double areaM2;
    private Long maxOccupants;

    /**
     * Các trường tài chính là optional — nếu null thì lấy từ Contract.
     */
    private Double rentPrice;
    private Double electricityPricePerKwh;
    private Double waterPricePerM3;
    private Double internetFee;
    private Double parkingFee;
    private Double cleaningFee;
}