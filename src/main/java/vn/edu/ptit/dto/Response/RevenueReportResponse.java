package vn.edu.ptit.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueReportResponse {
    private Integer year;
    private Integer month;
    private Double rentAmount;
    private Double utilityAmount;
    private Double totalRevenue;
    private Long paidCount;
    private Long unpaidCount;
}
