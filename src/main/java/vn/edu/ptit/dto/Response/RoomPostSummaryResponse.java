package vn.edu.ptit.dto.Response;

import lombok.Builder;
import lombok.Data;
import vn.edu.ptit.entity.RoomPosts;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response tóm tắt bài đăng — dùng cho danh sách tìm kiếm và card phòng trọ.
 * Không bao gồm description đầy đủ để giảm payload.
 */
@Data
@Builder
public class RoomPostSummaryResponse {

    private Long id;
    private String title;
    private BigDecimal monthlyRent;
    private Double areaM2;
    private String roomType;
    private RoomPosts.Status status;

    /** Ví dụ: "Quận Cầu Giấy, Hà Nội" */
    private String shortAddress;

    private String thumbnailUrl;
    private LocalDateTime publishedAt;

    private String landlordName;
    private String landlordAvatarUrl;
}
