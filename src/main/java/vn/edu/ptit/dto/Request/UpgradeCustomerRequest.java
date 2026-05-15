package vn.edu.ptit.dto.Request;

import lombok.Data;

@Data
public class UpgradeCustomerRequest {
    private String idCardNumber;
    private String address;
}