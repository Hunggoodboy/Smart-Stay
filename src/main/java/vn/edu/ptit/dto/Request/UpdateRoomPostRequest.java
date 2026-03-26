package vn.edu.ptit.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import vn.edu.ptit.entity.RoomPosts;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Request cập nhật bài đăng (chỉ các field được phép sửa sau khi đăng).
 * Dùng PATCH — null = giữ nguyên giá trị cũ.
 */
@Data
public class UpdateRoomPostRequest {

    @Size(min = 10, max = 200, message = "Tiêu đề phải từ 10 đến 200 ký tự")
    private String title;

    @Size(max = 5000)
    private String description;

    @DecimalMin(value = "0.0", inclusive = false, message = "Giá cho thuê phải lớn hơn 0")
    private BigDecimal postedPrice;

    private Boolean hasWifi;
    private Boolean hasAirConditioner;
    private Boolean hasWaterHeater;
    private Boolean hasParking;
    private Boolean hasSecurity;
    private Boolean hasElevator;
    private Boolean allowCooking;
    private Boolean allowPet;

    @Size(max = 500)
    private String extraAmenities;

    private BigDecimal electricityPricePerKwh;
    private BigDecimal waterPricePerM3;
    private Double internetFee;
    private Double parkingFee;

    /**
     * Nếu có giá trị → thay toàn bộ danh sách ảnh.
     * Nếu null → giữ nguyên danh sách ảnh hiện tại.
     */
    private List<CreateRoomPostRequest.ImageItem> images;

    private LocalDateTime expiredAt;

    /**
     * Thay đổi trạng thái: ACTIVE ↔ INACTIVE | DELETED
     */
    private RoomPosts.Status status;
}
