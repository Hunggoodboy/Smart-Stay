package vn.edu.ptit.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.ptit.dto.LandlordInfo;
import vn.edu.ptit.entity.RoomPosts;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response đầy đủ chi tiết bài đăng — dùng cho trang xem chi tiết.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RoomPostDetailResponse {

    private Long id;
    private String title;
    private String description;
    private BigDecimal monthlyRent;
    private BigDecimal depositAmount;
    private Double areaM2;
    private Integer maxOccupants;
    private String roomType;
    private RoomPosts.Status status;

    // ==================== ĐỊA CHỈ ====================

    private String address;
    private String ward;
    private String district;
    private String city;

    /** Địa chỉ đầy đủ đã ghép sẵn — hiển thị trên UI */
    private String fullAddress;

    // ==================== GIÁ DỊCH VỤ ====================

    private BigDecimal electricityPricePerKwh;
    private BigDecimal waterPricePerM3;
    private Double internetFee;
    private Double parkingFee;
    private Double cleaningFee;

    // ==================== THỜI GIAN ====================

    private LocalDateTime createdAt;

    // ==================== CHỦ NHÀ ====================

    private LandlordInfo landlord;

    // ==================== ẢNH ====================

    private List<String> images;

    /** URL ảnh đại diện — lấy nhanh không cần duyệt images */
    private String mainImageUrl;

    // ==================== TRẠNG THÁI YÊU CẦU THUÊ CỦA NGƯỜI DÙNG HIỆN TẠI ====================

    /**
     * null  — chưa đăng nhập hoặc chưa gửi yêu cầu
     * Khác  — trạng thái yêu cầu thuê gần nhất của customer đang đăng nhập
     */
    private String myRequestStatus;

    // ==================== Sở hữu ====================

    private boolean isOwner;

}
