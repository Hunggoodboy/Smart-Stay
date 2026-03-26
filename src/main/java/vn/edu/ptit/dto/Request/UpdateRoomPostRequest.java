package vn.edu.ptit.dto.Request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import vn.edu.ptit.entity.RoomPosts;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Request cập nhật bài đăng — kiểu PATCH (null = giữ nguyên giá trị cũ).
 * Chỉ được phép cập nhật khi status = DRAFT hoặc ACTIVE.
 * Không thể sửa bài đăng đang ở trạng thái RENTED hoặc DELETED.
 */
@Data
public class UpdateRoomPostRequest {

    @Size(min = 10, max = 200, message = "Tiêu đề phải từ 10 đến 200 ký tự")
    private String title;

    @Size(max = 5000)
    private String description;

    @DecimalMin(value = "0.0", inclusive = false, message = "Giá thuê phải lớn hơn 0")
    private BigDecimal monthlyRent;

    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal depositAmount;

    @Positive
    private Double areaM2;

    @Min(value = 1)
    private Integer maxOccupants;

    private String roomType;

    private String address;
    private String ward;
    private String district;
    private String city;

    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal electricityPricePerKwh;

    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal waterPricePerM3;

    @PositiveOrZero
    private Double internetFee;

    @PositiveOrZero
    private Double parkingFee;

    @PositiveOrZero
    private Double cleaningFee;

    /**
     * Nếu không null → thay toàn bộ gallery bằng danh sách mới.
     * Nếu null → giữ nguyên gallery hiện tại.
     */
    @Size(max = 10, message = "Tối đa 10 ảnh mỗi bài đăng")
    @Valid
    private List<String> images;

    @Future(message = "Thời hạn bài đăng phải là thời điểm trong tương lai")
    private LocalDateTime expiredAt;

    /**
     * Chỉ cho phép chuyển: DRAFT ↔ ACTIVE, ACTIVE → INACTIVE, * → DELETED.
     * Service sẽ validate các chuyển trạng thái không hợp lệ.
     */
    private RoomPosts.Status status;
}
