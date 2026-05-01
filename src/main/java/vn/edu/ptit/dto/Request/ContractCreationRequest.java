package vn.edu.ptit.dto.Request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ContractCreationRequest {

    private Long landLordId;
    private Long customerId;
    private Long roomId;

    private Double monthlyRent;
    private Double depositAmount;
    private Double electricityRate;
    private Double waterRate;
    private Long billingDate;
    private Long numOccupants;

    // BỔ SUNG CÁC KHOẢN PHÍ MỚI TỪ FORM
    private Double internetFee;
    private Double parkingFee;
    private Double cleaningFee;

    private LocalDate startDate;
    private LocalDate endDate;
    private Integer paymentCycle;

    private String termsAndConditions;
}