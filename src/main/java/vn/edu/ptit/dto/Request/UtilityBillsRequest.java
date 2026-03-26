package vn.edu.ptit.dto.Request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UtilityBillsRequest {
    private Long roomId;
    private String billingMonth;       // "2025-03"

    private Double electricityOldIndex;
    private Double electricityNewIndex;

    private Double waterOldIndex;
    private Double waterNewIndex;

    // Các phí có thể override từ mặc định của phòng
    private Double internetFee;
    private Double parkingFee;
    private Double cleaningFee;
    private Double otherFee;
    private String otherFeeNote;

    private LocalDate dueDate;
    private String notes;
}
