package vn.edu.ptit.dto.response;

import lombok.Builder;
import lombok.Data;
import vn.edu.ptit.entity.RoomPosts;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response đầy đủ chi tiết bài đăng — dùng cho trang xem chi tiết.
 */
@Data
@Builder
public class RoomPostDetailResponse {

    private Long id;
    private String title;
    private String description;
    private BigDecimal postedPrice;
    private Double areaM2;
    private String roomType;
    private RoomPosts.Status status;

    // ==================== ĐỊA CHỈ ====================
    private String address;
    private String ward;
    private String district;
    private String city;
    private String fullAddress; // address + ward + district + city ghép lại

    // ==================== TIỆN ÍCH ====================
    private Boolean hasWifi;
    private Boolean hasAirConditioner;
    private Boolean hasWaterHeater;
    private Boolean hasParking;
    private Boolean hasSecurity;
    private Boolean hasElevator;
    private Boolean allowCooking;
    private Boolean allowPet;
    private String extraAmenities;

    // ==================== PHÍ DỊCH VỤ ====================
    private BigDecimal electricityPricePerKwh;
    private BigDecimal waterPricePerM3;
    private Double internetFee;
    private Double parkingFee;

    // ==================== THỐNG KÊ ====================
    private Long viewCount;
    private Long contactCount;

    // ==================== THỜI GIAN ====================
    private LocalDateTime publishedAt;
    private LocalDateTime expiredAt;
    private LocalDateTime createdAt;

    // ==================== CHỦ NHÀ ====================
    private LandlordSummary landlord;

    // ==================== ẢNH ====================
    private List<ImageResponse> images;
    private String thumbnailUrl; // ảnh đại diện (lấy nhanh)

    // ==================== TRẠNG THÁI YÊU CẦU THUÊ (nếu đã đăng nhập) ====================
    /** null nếu chưa đăng nhập hoặc chưa gửi yêu cầu */
    private String myRequestStatus;

    @Data
    @Builder
    public static class LandlordSummary {
        private Long id;
        private String fullName;
        private String phoneNumber;
        private String avatarUrl;
    }

    @Data
    @Builder
    public static class ImageResponse {
        private Long id;
        private String imageUrl;
        private Integer displayOrder;
        private Boolean thumbnail;
    }
}
