package vn.edu.ptit.dto.Response;


import lombok.Data;
import vn.edu.ptit.entity.UtilityBills;

import java.math.BigDecimal;
import java.time.LocalDateTime;

//Response gửi cho người thuê
@Data
public class UtilityBillsResponse {
    private Long billingMonth;
    private Double electricityOldIndex;
    private Double electricityNewIndex;
    private Double electricityConsumed;
    private BigDecimal electricityPricePerKwh;
    private BigDecimal electricityAmount; //Tien dien thang nay
    private Double waterOldIndex = 0.0;
    private Double waterNewIndex = 0.0;
    private Double waterConsumed;
    private Double waterPricePerM3;
    private BigDecimal waterAmount; // Tien nuoc thang nay
    private Double internetFee = 0.0;
    private Double parkingFee = 0.0;
    private Double cleaningFee = 0.0;
    private Double otherFee = 0.0;
    private String otherFeeNote;
    private BigDecimal totalAmount;
    private LocalDateTime dueDate; // Hạn đóng tiền
    private LocalDateTime createdAt;
    private UtilityBills.Status status;

    public UtilityBillsResponse fromEntity(UtilityBills entity) {
        UtilityBillsResponse detailResponse = new UtilityBillsResponse();
        detailResponse.setBillingMonth(entity.getBillingMonth());
        detailResponse.setElectricityOldIndex(entity.getElectricityOldIndex());
        detailResponse.setElectricityNewIndex(entity.getElectricityNewIndex());
        detailResponse.setElectricityConsumed(entity.getElectricityConsumed());
        detailResponse.setElectricityPricePerKwh(entity.getElectricityPricePerKwh());
        detailResponse.setElectricityAmount(entity.getElectricityAmount());
        detailResponse.setWaterOldIndex(entity.getWaterOldIndex());
        detailResponse.setWaterNewIndex(entity.getWaterNewIndex());
        detailResponse.setWaterConsumed(entity.getWaterConsumed());
        detailResponse.setWaterPricePerM3(entity.getWaterPricePerM3());
        detailResponse.setWaterAmount(entity.getWaterAmount());
        detailResponse.setInternetFee(entity.getInternetFee());
        detailResponse.setParkingFee(entity.getParkingFee());
        detailResponse.setCleaningFee(entity.getCleaningFee());
        detailResponse.setOtherFee(entity.getOtherFee());
        detailResponse.setOtherFeeNote(entity.getOtherFeeNote());
        detailResponse.setTotalAmount(entity.getTotalAmount());
        detailResponse.setCreatedAt(entity.getCreatedAt());
        detailResponse.setDueDate(entity.getDueDate());
        detailResponse.setStatus(entity.getStatus());
        detailResponse.setTotalAmount(entity.getTotalAmount());
        return detailResponse;
    }

}
