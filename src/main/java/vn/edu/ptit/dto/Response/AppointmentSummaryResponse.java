package vn.edu.ptit.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.ptit.entity.Appointments;

import java.time.LocalDateTime;

/**
 * Response tóm tắt lịch hẹn — dùng cho hiển thị danh sách lịch hẹn (list).
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AppointmentSummaryResponse {

    private Long id;
    private LocalDateTime appointmentTime;
    private String location;
    private Appointments.Status status;
    
    // Thông tin bài đăng
    private Long roomPostId;
    private String roomPostTitle;
    private String roomPostMainImageUrl;

    // Thông tin chủ nhà
    private Long landlordId;
    private String landlordName;
    private String landlordAvatarUrl;

    // Thông tin người thuê (khách hàng)
    private Long customerId;
    private String customerName;
    private String customerAvatarUrl;

    private LocalDateTime createdAt;
}
