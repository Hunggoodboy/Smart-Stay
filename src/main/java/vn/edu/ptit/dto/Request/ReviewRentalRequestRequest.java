package vn.edu.ptit.dto.Request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import vn.edu.ptit.entity.RentalRequests;

/**
 * Request chủ nhà duyệt hoặc từ chối yêu cầu thuê.
 *
 * Khi action = APPROVED:
 *   Service sẽ tự động:
 *     1. Tạo Rooms mới từ thông tin RoomPost
 *     2. Tạo Contracts liên kết Rooms + Customer + LandLord
 *     3. Chuyển RentalRequest.status → CONTRACTED
 *     4. Chuyển RoomPost.status → RENTED, gán room_id
 *     5. Huỷ (CANCELLED) tất cả yêu cầu PENDING còn lại của bài đăng đó
 *
 * Khi action = REJECTED:
 *   rejectionReason nên được điền để thông báo cho khách hàng.
 */
@Data
public class ReviewRentalRequestRequest {

    @NotNull(message = "Vui lòng chọn hành động")
    private RentalRequests.Status action; // Chỉ chấp nhận APPROVED hoặc REJECTED

    @Size(max = 500, message = "Lý do từ chối không được vượt quá 500 ký tự")
    private String rejectionReason;

    /** Ghi chú nội bộ — không hiển thị cho khách hàng */
    @Size(max = 500)
    private String landlordNotes;
}
