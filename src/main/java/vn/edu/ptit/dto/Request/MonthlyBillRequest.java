package vn.edu.ptit.dto.Request;

import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonFormat;

public class MonthlyBillRequest {

    private Long roomId;
    private Long contractId;
    private String billingMonth;

    private Double electricityOldIndex;
    private Double electricityNewIndex;
    private Double electricityPricePerKwh;

    private Double waterOldIndex;
    private Double waterNewIndex;
    private Double waterPricePerM3;

    private Double internetFee;
    private Double parkingFee;
    private Double cleaningFee;
    private Double otherFee;

    private String otherFeeNote;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dueDate;
    private String notes;

    public MonthlyBillRequest() {
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public Long getContractId() {
        return contractId;
    }

    public void setContractId(Long contractId) {
        this.contractId = contractId;
    }

    public String getBillingMonth() {
        return billingMonth;
    }

    public void setBillingMonth(String billingMonth) {
        this.billingMonth = billingMonth;
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

    public Double getElectricityPricePerKwh() {
        return electricityPricePerKwh;
    }

    public void setElectricityPricePerKwh(Double electricityPricePerKwh) {
        this.electricityPricePerKwh = electricityPricePerKwh;
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

    public Double getWaterPricePerM3() {
        return waterPricePerM3;
    }

    public void setWaterPricePerM3(Double waterPricePerM3) {
        this.waterPricePerM3 = waterPricePerM3;
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

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}