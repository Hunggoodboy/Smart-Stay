package vn.edu.ptit.dto.Request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Request gửi yêu cầu hẹn lịch (xem phòng) từ phía chủ nhà hoặc khách hàng.
 */
@Data
public class AppointmentRequest {

    @NotNull(message = "ID yêu cầu thuê không được để trống")
    private Long rentalRequestId;

    @NotNull(message = "Thời gian hẹn không được để trống")
    @Future(message = "Thời gian hẹn phải là thời gian trong tương lai")
    private LocalDateTime appointmentTime;

    @NotBlank(message = "Địa điểm không được để trống")
    @Size(max = 255, message = "Địa điểm không được vượt quá 255 ký tự")
    private String location;

    @Size(max = 1000, message = "Ghi chú không được vượt quá 1000 ký tự")
    private String note;
}
