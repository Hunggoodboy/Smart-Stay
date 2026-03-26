package vn.edu.ptit.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

/**
 * Request gửi yêu cầu thuê phòng từ phía Customer.
 */
@Data
public class CreateRentalRequestRequest {

    @NotNull(message = "Vui lòng chọn bài đăng muốn thuê")
    private Long roomPostId;

    @Size(max = 1000, message = "Lời nhắn không được vượt quá 1000 ký tự")
    private String message;

    @Future(message = "Ngày chuyển vào phải là ngày trong tương lai")
    private LocalDate desiredMoveInDate;

    @Min(value = 1, message = "Thời gian thuê tối thiểu 1 tháng")
    @Max(value = 60, message = "Thời gian thuê tối đa 60 tháng")
    private Integer desiredDurationMonths;

    @NotNull(message = "Vui lòng nhập số người ở")
    @Min(value = 1, message = "Số người ở tối thiểu là 1")
    @Max(value = 20, message = "Số người ở tối đa là 20")
    private Integer numOccupants = 1;
}
