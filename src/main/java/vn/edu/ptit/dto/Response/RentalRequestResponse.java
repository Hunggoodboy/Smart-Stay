package vn.edu.ptit.dto.Response;

import lombok.Builder;
import lombok.Data;
import vn.edu.ptit.entity.RentalRequests;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response chi tiết yêu cầu thuê phòng.
 * Dùng chung cho cả Customer (xem lịch sử) và Landlord (xem danh sách yêu cầu).
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

    /** Chỉ Landlord mới thấy field này */
    private String landlordNotes;

    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ==================== BÀI ĐĂNG LIÊN QUAN ====================

    private RoomPostInfo roomPost;

    // ==================== THÔNG TIN KHÁCH HÀNG (Landlord xem) ====================

    private CustomerInfo customer;

    // ==================== THÔNG TIN CHỦ NHÀ (Customer xem) ====================

    private LandlordInfo landlord;

    // ==================== HỢP ĐỒNG ĐÃ TẠO ====================

    /**
     * Chỉ có giá trị khi status = CONTRACTED.
     * Landlord / Customer dùng contractId để điều hướng sang màn quản lý.
     */
    private Long contractId;
    private String contractCode;

    // ==================== INNER CLASSES ====================

    @Data
    @Builder
    public static class RoomPostInfo {
        private Long id;
        private String title;
        private String thumbnailUrl;
        private BigDecimal monthlyRent;
        private String shortAddress;
        private String roomType;
    }

    @Data
    @Builder
    public static class CustomerInfo {
        private Long id;
        private String fullName;
        private String phoneNumber;
        private String email;
        private String avatarUrl;
        private String idCardNumber;
    }

    @Data
    @Builder
    public static class LandlordInfo {
        private Long id;
        private String fullName;
        private String phoneNumber;
        private String email;
        private String avatarUrl;
    }
}
