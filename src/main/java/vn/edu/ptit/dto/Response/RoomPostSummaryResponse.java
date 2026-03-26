package vn.edu.ptit.dto.response;

import lombok.Builder;
import lombok.Data;
import vn.edu.ptit.entity.RoomPosts;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response tóm tắt bài đăng — dùng cho danh sách tìm kiếm, card phòng trọ.
 * Không bao gồm description đầy đủ và danh sách ảnh để giảm payload.
 */
@Data
@Builder
public class RoomPostSummaryResponse {

    private Long id;
    private String title;
    private BigDecimal postedPrice;
    private Double areaM2;
    private String roomType;
    private RoomPosts.Status status;

    private String district;
    private String city;
    private String shortAddress; // "Quận Cầu Giấy, Hà Nội"

    private String thumbnailUrl;

    // Tiện ích nổi bật (hiển thị dưới dạng icon)
    private Boolean hasWifi;
    private Boolean hasAirConditioner;
    private Boolean hasParking;
    private Boolean allowPet;

    private Long viewCount;
    private LocalDateTime publishedAt;

    // Thông tin chủ nhà ngắn gọn
    private String landlordName;
    private String landlordAvatarUrl;
}
