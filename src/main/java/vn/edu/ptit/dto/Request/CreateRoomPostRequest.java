package vn.edu.ptit.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Request tạo bài đăng cho thuê phòng.
 * Landlord chọn phòng đã có trong hệ thống, sau đó tuỳ chỉnh thông tin hiển thị.
 */
@Data
public class CreateRoomPostRequest {

    @NotNull(message = "Vui lòng chọn phòng cần đăng")
    private Long roomId;

    @NotBlank(message = "Tiêu đề bài đăng không được để trống")
    @Size(min = 10, max = 200, message = "Tiêu đề phải từ 10 đến 200 ký tự")
    private String title;

    @Size(max = 5000, message = "Mô tả không được vượt quá 5000 ký tự")
    private String description;

    @NotNull(message = "Giá cho thuê không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá cho thuê phải lớn hơn 0")
    private BigDecimal postedPrice;

    // ==================== ĐỊA CHỈ ====================
    // Nếu null thì lấy từ Rooms, nếu có thì override

    private String address;
    private String ward;
    private String district;
    private String city;

    // ==================== TIỆN ÍCH ====================

    private Boolean hasWifi = false;
    private Boolean hasAirConditioner = false;
    private Boolean hasWaterHeater = false;
    private Boolean hasParking = false;
    private Boolean hasSecurity = false;
    private Boolean hasElevator = false;
    private Boolean allowCooking = false;
    private Boolean allowPet = false;

    @Size(max = 500, message = "Tiện ích bổ sung không được vượt quá 500 ký tự")
    private String extraAmenities;

    // ==================== PHÍ DỊCH VỤ ====================

    private BigDecimal electricityPricePerKwh;
    private BigDecimal waterPricePerM3;
    private Double internetFee;
    private Double parkingFee;

    // ==================== ẢNH ====================

    /**
     * Danh sách URL ảnh đã upload (xử lý upload riêng trước khi gọi API này)
     */
    @Size(max = 10, message = "Tối đa 10 ảnh cho mỗi bài đăng")
    private List<ImageItem> images;

    /**
     * Thời điểm hết hạn bài đăng (null = không giới hạn)
     */
    private LocalDateTime expiredAt;

    /**
     * true = publish ngay, false = lưu nháp
     */
    private Boolean publishImmediately = false;

    @Data
    public static class ImageItem {
        @NotBlank
        private String imageUrl;
        private Integer displayOrder = 0;
        private Boolean thumbnail = false;
    }
}
