package vn.edu.ptit.dto.Request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpgradeCustomerRequest {
    private String idCardNumber;
    private String address;
}