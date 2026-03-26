package vn.edu.ptit.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import vn.edu.ptit.entity.RentalRequests;

/**
 * Request chủ nhà duyệt hoặc từ chối yêu cầu thuê.
 * action = APPROVED → hệ thống tiếp tục tạo Contract.
 * action = REJECTED → cần có rejectionReason.
 */
@Data
public class ReviewRentalRequestRequest {

    @NotNull(message = "Vui lòng chọn hành động")
    private RentalRequests.Status action; // Chỉ chấp nhận APPROVED hoặc REJECTED

    @Size(max = 500, message = "Lý do từ chối không được vượt quá 500 ký tự")
    private String rejectionReason;

    @Size(max = 500)
    private String landlordNotes;
}
