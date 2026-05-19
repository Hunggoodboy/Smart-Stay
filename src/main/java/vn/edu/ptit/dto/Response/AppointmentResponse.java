package vn.edu.ptit.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.ptit.entity.Appointments;

import java.time.LocalDateTime;

/**
 * Response chi tiết lịch hẹn — dùng cho trang xem chi tiết lịch hẹn (detail).
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AppointmentResponse {

    private Long id;
    private LocalDateTime appointmentTime;
    private String location;
    private String note;
    private Appointments.Status status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Thông tin yêu cầu thuê liên quan
    private Long rentalRequestId;

    // Thông tin bài đăng phòng trọ
    private Long roomPostId;
    private String roomPostTitle;
    private String roomPostAddress;
    private String roomPostMainImageUrl;

    // Thông tin chủ nhà
    private Long landlordId;
    private String landlordName;
    private String landlordPhone;
    private String landlordEmail;
    private String landlordAvatarUrl;

    // Thông tin người thuê (khách hàng)
    private Long customerId;
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private String customerAvatarUrl;
}
