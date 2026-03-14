package vn.edu.ptit.dto;

import lombok.Data;
import vn.edu.ptit.entity.UtilityBills;

import java.math.BigDecimal;
import java.time.LocalDateTime;

//Respone tóm tắt người cho thuê xem các hoá đơn
@Data
public class UtilityBillsRespone {
    private BigDecimal electricityAmount; // tiền điện
    private BigDecimal waterAmount; // tiền nước
    private BigDecimal ServiceAmount; // tiền dịch vụ
    private UtilityBills.Status status; // trạng thái đóng tiền người thuê, nộp hay chưa
    private LocalDateTime createdAt; // được tạo vào
    private LocalDateTime dueDate; //Hạn nộp tiền
    private BigDecimal totalAmount; // Tổng tiền

    public UtilityBillsRespone fromEntity(UtilityBills entity) {
        UtilityBillsRespone billsRespone = new UtilityBillsRespone();
        billsRespone.setElectricityAmount(entity.getElectricityAmount());
        billsRespone.setWaterAmount(entity.getWaterAmount());
        billsRespone.setServiceAmount(BigDecimal.valueOf(entity.getInternetFee() + entity.getCleaningFee() + entity.getParkingFee() + entity.getOtherFee()));
        billsRespone.setStatus(entity.getStatus());
        billsRespone.setCreatedAt(entity.getCreatedAt());
        billsRespone.setDueDate(entity.getDueDate());
        billsRespone.setTotalAmount(entity.getTotalAmount());
        return billsRespone;
    }
}
