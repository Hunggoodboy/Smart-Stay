package vn.edu.ptit.dto.Response;

import lombok.Builder;
import lombok.Data;
import vn.edu.ptit.entity.RentalRequests;

import java.time.LocalDateTime;

/**
 * Response rút gọn cho yêu cầu thuê phòng.
 * Đã lược bỏ các field không có trong Entity và tinh giản thông tin liên quan.
 */
@Data
@Builder
public class RentalRequestResponse {

    private Long id;
    private RentalRequests.Status status;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;

    // ==================== THÔNG TIN TINH GIẢN ====================

    private RoomPostInfo roomPost;
    private UserInfo customer;
    private UserInfo landlord;
    private Long contractId; // Trả về ID để FE tiện điều hướng

    // ==================== INNER CLASSES ====================

    @Data
    @Builder
    public static class RoomPostInfo {
        private Long id;
        private String title;       // Tên phòng
        private String thumbnailUrl; // Ảnh chính
    }

    @Data
    @Builder
    public static class UserInfo {
        private Long id;
        private String fullName;
        private String avatarUrl;
    }
}