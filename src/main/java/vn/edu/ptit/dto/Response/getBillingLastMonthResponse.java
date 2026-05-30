package vn.edu.ptit.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class getBillingLastMonthResponse {
    private Double oldElectronic;
    private Double oldWater;
}
