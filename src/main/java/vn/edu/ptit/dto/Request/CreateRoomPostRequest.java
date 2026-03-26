package vn.edu.ptit.dto.Request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Request tạo bài đăng cho thuê phòng.
 * Landlord điền thông tin từ đầu — chưa cần chọn Rooms có sẵn.
 * publishImmediately = true → status = ACTIVE, false → status = DRAFT.
 */
@Data
public class CreateRoomPostRequest {

    // ==================== THÔNG TIN CƠ BẢN ====================

    @NotBlank(message = "Tiêu đề không được để trống")
    @Size(min = 10, max = 200, message = "Tiêu đề phải từ 10 đến 200 ký tự")
    private String title;

    @Size(max = 5000, message = "Mô tả không được vượt quá 5000 ký tự")
    private String description;

    @NotNull(message = "Giá thuê không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá thuê phải lớn hơn 0")
    private BigDecimal monthlyRent;

    @DecimalMin(value = "0.0", inclusive = false, message = "Tiền cọc phải lớn hơn 0")
    private BigDecimal depositAmount;

    @Positive(message = "Diện tích phải lớn hơn 0")
    private Double areaM2;

    @Min(value = 1, message = "Số người ở tối thiểu là 1")
    private Integer maxOccupants;

    private String roomType;

    // ==================== ĐỊA CHỈ ====================

    @NotBlank(message = "Địa chỉ không được để trống")
    private String address;

    private String ward;
    private String district;

    @NotBlank(message = "Thành phố không được để trống")
    private String city;

    // ==================== GIÁ DỊCH VỤ ====================

    @DecimalMin(value = "0.0", inclusive = false, message = "Giá điện phải lớn hơn 0")
    private BigDecimal electricityPricePerKwh;

    @DecimalMin(value = "0.0", inclusive = false, message = "Giá nước phải lớn hơn 0")
    private BigDecimal waterPricePerM3;

    @PositiveOrZero(message = "Phí internet không được âm")
    private Double internetFee;

    @PositiveOrZero(message = "Phí giữ xe không được âm")
    private Double parkingFee;

    @PositiveOrZero(message = "Phí vệ sinh không được âm")
    private Double cleaningFee;

    // ==================== ẢNH ====================

    private String mainImageUrl;

    /**
     * Danh sách ảnh đã upload (URL từ bước upload trước).
     * Tối đa 10 ảnh; đúng 1 ảnh phải có thumbnail = true.
     */
    @Size(max = 10, message = "Tối đa 10 ảnh mỗi bài đăng")
    @Valid
    private List<String> imageUrl;

    // ==================== INNER CLASS ====================

}
