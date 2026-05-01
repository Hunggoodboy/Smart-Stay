package vn.edu.ptit.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RoomManageSummaryResponse {
    // Định danh phòng
    private Long id;
    private String roomNumber;
    private String roomType;

    // Tài chính và Trạng thái[cite: 1]
    private Double rentPrice;
    private String status;

    // Thông tin người mua/người thuê (nếu phòng đang ở trạng thái RENTED)
    private String userName;
    private String userEmail;
}