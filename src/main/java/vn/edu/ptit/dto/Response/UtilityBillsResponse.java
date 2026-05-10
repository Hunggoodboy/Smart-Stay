package vn.edu.ptit.dto.Response;

import lombok.Data;
import vn.edu.ptit.entity.Rooms;
import vn.edu.ptit.entity.UtilityBills;

import java.time.LocalDate;
import java.time.LocalDateTime;

//Response gửi cho người thuê
@Data
public class UtilityBillsResponse {
    private String billingMonth;
    private Double electricityOldIndex;
    private Double electricityNewIndex;
    private Double electricityConsumed;
    private Double electricityPricePerKwh;
    private Double electricityAmount; // Tien dien thang nay
    private Double waterOldIndex = 0.0;
    private Double waterNewIndex = 0.0;
    private Double waterConsumed;
    private Double waterPricePerM3;
    private Double waterAmount; // Tien nuoc thang nay
    private Double internetFee = 0.0;
    private Double parkingFee = 0.0;
    private Double cleaningFee = 0.0;
    private Double otherFee = 0.0;
    private Double rentPrice;
    private String otherFeeNote;
    private Double totalAmount;
    private LocalDate dueDate; // Hạn đóng tiền
    private LocalDateTime createdAt;
    private UtilityBills.Status status;
    private String roomName;
    private String roomAddress;

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

        Double rent = 0.0;
        if (entity.getContract() != null && entity.getContract().getMonthlyRent() != null) {
            rent = entity.getContract().getMonthlyRent();
        } else if (entity.getRoom() != null && entity.getRoom().getRentPrice() != null) {
            rent = entity.getRoom().getRentPrice();
        }
        detailResponse.setRentPrice(rent);

        Double utilTotal = entity.getTotalAmount() != null ? entity.getTotalAmount() : 0.0;
        detailResponse.setTotalAmount(utilTotal + rent);

        detailResponse.setCreatedAt(entity.getCreatedAt());
        detailResponse.setDueDate(entity.getDueDate());
        detailResponse.setStatus(entity.getStatus());

        if (entity.getRoom() != null) {
            detailResponse.setRoomName(entity.getRoom().getRoomNumber());

            Rooms room = entity.getRoom();
            StringBuilder addressBuilder = new StringBuilder();
            if (room.getAddress() != null && !room.getAddress().isEmpty()) {
                addressBuilder.append(room.getAddress());
            }
            if (room.getWard() != null && !room.getWard().isEmpty()) {
                if (addressBuilder.length() > 0)
                    addressBuilder.append(", ");
                addressBuilder.append(room.getWard());
            }
            if (room.getDistrict() != null && !room.getDistrict().isEmpty()) {
                if (addressBuilder.length() > 0)
                    addressBuilder.append(", ");
                addressBuilder.append(room.getDistrict());
            }
            if (room.getCity() != null && !room.getCity().isEmpty()) {
                if (addressBuilder.length() > 0)
                    addressBuilder.append(", ");
                addressBuilder.append(room.getCity());
            }
            detailResponse.setRoomAddress(addressBuilder.toString());
        }

        return detailResponse;
    }

}
