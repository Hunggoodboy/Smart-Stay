package vn.edu.ptit.dto.Response;

import java.time.LocalDate;

public class MonthlyBillResponse {

    private Long utilityBillId;
    private Long rentPaymentId;

    private Long roomId;
    private String roomNumber;
    private Long contractId;
    private String contractCode;
    private String billingMonth;

    private Double rentAmount;

    private Double electricityOldIndex;
    private Double electricityNewIndex;
    private Double electricityConsumed;
    private Double electricityPricePerKwh;
    private Double electricityAmount;

    private Double waterOldIndex;
    private Double waterNewIndex;
    private Double waterConsumed;
    private Double waterPricePerM3;
    private Double waterAmount;

    private Double internetFee;
    private Double parkingFee;
    private Double cleaningFee;
    private Double otherFee;
    private Double serviceAmount;
    private Double utilityAmount;
    private Double totalAmount;

    private String otherFeeNote;
    private LocalDate dueDate;
    private String status;
    private String notes;
    private String message;

    public MonthlyBillResponse() {
    }

    public Long getUtilityBillId() {
        return utilityBillId;
    }

    public void setUtilityBillId(Long utilityBillId) {
        this.utilityBillId = utilityBillId;
    }

    public Long getRentPaymentId() {
        return rentPaymentId;
    }

    public void setRentPaymentId(Long rentPaymentId) {
        this.rentPaymentId = rentPaymentId;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public Long getContractId() {
        return contractId;
    }

    public void setContractId(Long contractId) {
        this.contractId = contractId;
    }

    public String getContractCode() {
        return contractCode;
    }

    public void setContractCode(String contractCode) {
        this.contractCode = contractCode;
    }

    public String getBillingMonth() {
        return billingMonth;
    }

    public void setBillingMonth(String billingMonth) {
        this.billingMonth = billingMonth;
    }

    public Double getRentAmount() {
        return rentAmount;
    }

    public void setRentAmount(Double rentAmount) {
        this.rentAmount = rentAmount;
    }

    public Double getElectricityOldIndex() {
        return electricityOldIndex;
    }

    public void setElectricityOldIndex(Double electricityOldIndex) {
        this.electricityOldIndex = electricityOldIndex;
    }

    public Double getElectricityNewIndex() {
        return electricityNewIndex;
    }

    public void setElectricityNewIndex(Double electricityNewIndex) {
        this.electricityNewIndex = electricityNewIndex;
    }

    public Double getElectricityConsumed() {
        return electricityConsumed;
    }

    public void setElectricityConsumed(Double electricityConsumed) {
        this.electricityConsumed = electricityConsumed;
    }

    public Double getElectricityPricePerKwh() {
        return electricityPricePerKwh;
    }

    public void setElectricityPricePerKwh(Double electricityPricePerKwh) {
        this.electricityPricePerKwh = electricityPricePerKwh;
    }

    public Double getElectricityAmount() {
        return electricityAmount;
    }

    public void setElectricityAmount(Double electricityAmount) {
        this.electricityAmount = electricityAmount;
    }

    public Double getWaterOldIndex() {
        return waterOldIndex;
    }

    public void setWaterOldIndex(Double waterOldIndex) {
        this.waterOldIndex = waterOldIndex;
    }

    public Double getWaterNewIndex() {
        return waterNewIndex;
    }

    public void setWaterNewIndex(Double waterNewIndex) {
        this.waterNewIndex = waterNewIndex;
    }

    public Double getWaterConsumed() {
        return waterConsumed;
    }

    public void setWaterConsumed(Double waterConsumed) {
        this.waterConsumed = waterConsumed;
    }

    public Double getWaterPricePerM3() {
        return waterPricePerM3;
    }

    public void setWaterPricePerM3(Double waterPricePerM3) {
        this.waterPricePerM3 = waterPricePerM3;
    }

    public Double getWaterAmount() {
        return waterAmount;
    }

    public void setWaterAmount(Double waterAmount) {
        this.waterAmount = waterAmount;
    }

    public Double getInternetFee() {
        return internetFee;
    }

    public void setInternetFee(Double internetFee) {
        this.internetFee = internetFee;
    }

    public Double getParkingFee() {
        return parkingFee;
    }

    public void setParkingFee(Double parkingFee) {
        this.parkingFee = parkingFee;
    }

    public Double getCleaningFee() {
        return cleaningFee;
    }

    public void setCleaningFee(Double cleaningFee) {
        this.cleaningFee = cleaningFee;
    }

    public Double getOtherFee() {
        return otherFee;
    }

    public void setOtherFee(Double otherFee) {
        this.otherFee = otherFee;
    }

    public Double getServiceAmount() {
        return serviceAmount;
    }

    public void setServiceAmount(Double serviceAmount) {
        this.serviceAmount = serviceAmount;
    }

    public Double getUtilityAmount() {
        return utilityAmount;
    }

    public void setUtilityAmount(Double utilityAmount) {
        this.utilityAmount = utilityAmount;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getOtherFeeNote() {
        return otherFeeNote;
    }

    public void setOtherFeeNote(String otherFeeNote) {
        this.otherFeeNote = otherFeeNote;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}