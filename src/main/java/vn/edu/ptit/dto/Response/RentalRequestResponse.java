package vn.edu.ptit.dto.response;

import lombok.Builder;
import lombok.Data;
import vn.edu.ptit.entity.RentalRequests;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response chi tiết yêu cầu thuê phòng.
 * Dùng cho cả hai phía: Customer xem lịch sử, Landlord xem danh sách yêu cầu.
 */
@Data
@Builder
public class RentalRequestResponse {

    private Long id;
    private RentalRequests.Status status;
    private String message;
    private LocalDate desiredMoveInDate;
    private Integer desiredDurationMonths;
    private Integer numOccupants;
    private String rejectionReason;
    private String landlordNotes;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ==================== BÀI ĐĂNG LIÊN QUAN ====================
    private RoomPostSummary roomPost;

    // ==================== THÔNG TIN KHÁCH HÀNG (Landlord xem) ====================
    private CustomerSummary customer;

    // ==================== THÔNG TIN CHỦ NHÀ (Customer xem) ====================
    private LandlordSummary landlord;

    // ==================== HỢP ĐỒNG ĐÃ TẠO (nếu có) ====================
    /** Chỉ có giá trị khi status = CONTRACTED */
    private Long contractId;
    private String contractCode;

    // ==================== INNER CLASSES ====================

    @Data
    @Builder
    public static class RoomPostSummary {
        private Long id;
        private String title;
        private String thumbnailUrl;
        private BigDecimal postedPrice;
        private String shortAddress;
        private String roomType;
    }

    @Data
    @Builder
    public static class CustomerSummary {
        private Long id;
        private String fullName;
        private String phoneNumber;
        private String email;
        private String avatarUrl;
        private String idCardNumber;
    }

    @Data
    @Builder
    public static class LandlordSummary {
        private Long id;
        private String fullName;
        private String phoneNumber;
        private String email;
        private String avatarUrl;
    }
}
