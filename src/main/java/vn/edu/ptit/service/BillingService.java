package vn.edu.ptit.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.List;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.edu.ptit.dto.Response.SuggestionBillingResponse;
import vn.edu.ptit.entity.*;
import vn.edu.ptit.dto.Request.MonthlyBillRequest;
import vn.edu.ptit.dto.Response.MonthlyBillResponse;
import vn.edu.ptit.repository.*;
import vn.edu.ptit.service.Authentication.AuthService;

@Service
@Transactional
@AllArgsConstructor
public class BillingService {

    private final RoomsRepository roomsRepository;
    private final ContractsRepository contractsRepository;
    private final UtilityBillsRepository utilityBillsRepository;
    private final RentPaymentsRepository rentPaymentsRepository;
    private final AuthService authService;
    private final UserRepository userRepository;
    @Transactional(readOnly = true)
    public MonthlyBillResponse previewBill(MonthlyBillRequest request) {
        CalculationResult result = calculate(request);
        return buildResponse(result, null, null, "PREVIEW", "Tính thử hóa đơn thành công");
    }

    public MonthlyBillResponse createBill(MonthlyBillRequest request) {
        CalculationResult result = calculate(request);
        if (result.contract == null) {
            throw new RuntimeException("Không tìm thấy hợp đồng ACTIVE cho phòng " + result.room.getRoomNumber());
        }

        utilityBillsRepository
                .findTopByRoom_IdAndBillingMonthOrderByCreatedAtDesc(result.room.getId(), result.billingMonth)
                .ifPresent(existing -> {
                    throw new RuntimeException("Phòng đã có hóa đơn điện nước tháng " + result.billingMonth);
                });

//        Optional<UtilityBills> existingBill =
//                utilityBillsRepository.findTopByRoom_IdAndBillingMonthOrderByCreatedAtDesc(
//                        result.room.getId(),
//                        result.billingMonth
//                );
//
//        if (existingBill.isPresent()) {
//            throw new RuntimeException("Phòng đã có hóa đơn điện nước tháng " + result.billingMonth);
//        }

        rentPaymentsRepository
                .findTopByContract_IdAndBillingMonthOrderByCreatedAtDesc(result.contract.getId(), result.billingMonth)
                .ifPresent(existing -> {
                    throw new RuntimeException("Hợp đồng đã có hóa đơn tổng tháng " + result.billingMonth);
                });


        UtilityBills utilityBill = new UtilityBills();
        utilityBill.setRoom(result.room);
        utilityBill.setContract(result.contract);
        utilityBill.setBillingMonth(result.billingMonth);
        utilityBill.setLandLord(authService.getCurrentLandLord());
        utilityBill.setElectricityOldIndex(result.electricityOldIndex);
        utilityBill.setElectricityNewIndex(result.electricityNewIndex);
        utilityBill.setElectricityConsumed(result.electricityConsumed);
        utilityBill.setElectricityPricePerKwh(toMoney(result.electricityPricePerKwh));
        utilityBill.setElectricityAmount(toMoney(result.electricityAmount));
        utilityBill.setWaterOldIndex(result.waterOldIndex);
        utilityBill.setWaterNewIndex(result.waterNewIndex);
        utilityBill.setWaterConsumed(result.waterConsumed);
        utilityBill.setWaterPricePerM3(result.waterPricePerM3);
        utilityBill.setWaterAmount(result.waterAmount);

        utilityBill.setInternetFee(result.internetFee);
        utilityBill.setParkingFee(result.parkingFee);
        utilityBill.setCleaningFee(result.cleaningFee);
        utilityBill.setOtherFee(result.otherFee);
        utilityBill.setOtherFeeNote(result.otherFeeNote);
        utilityBill.setTotalAmount(result.utilityAmount);
        utilityBill.setDueDate(result.dueDate);
        utilityBill.setStatus(UtilityBills.Status.UNPAID);
        utilityBill.setNotes(result.notes);

        utilityBill = utilityBillsRepository.save(utilityBill);

        User currentCustomer = userRepository.findById(result.contract.getCustomer().getId()).orElseThrow(() -> new RuntimeException("Không tìm thấy customer"));


        RentPayments rentPayment = new RentPayments();
        rentPayment.setContract(result.contract);
        rentPayment.setUtilityBill(utilityBill);
        rentPayment.setBillingMonth(result.billingMonth);

        // Users hiện chưa có CCCD nên để null
        rentPayment.setIdCardNumber(null);
        rentPayment.setCustomer(currentCustomer);
        rentPayment.setRentAmount(result.rentAmount);
        rentPayment.setUtilityAmount(result.utilityAmount);
        rentPayment.setTotalAmount(result.totalAmount);
        rentPayment.setDueDate(result.dueDate);
        rentPayment.setLateFee(0.0);
        rentPayment.setStatus(RentPayments.Status.valueOf("UNPAID"));
        rentPayment.setNotes(result.notes);
        rentPayment.setRoom(result.room);

        rentPayment = rentPaymentsRepository.save(rentPayment);

        utilityBill.setRentPayment(rentPayment);

        return buildResponse(result, utilityBill, rentPayment, "UNPAID", "Tạo hóa đơn tháng thành công");
    }

    private CalculationResult calculate(MonthlyBillRequest request) {
        validateRequest(request);
        Rooms room = roomsRepository.findById(request.getRoomId())
                                    .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng với id = " + request.getRoomId()));
//        Optional<Rooms> roomot =roomsRepository.findById(request.getRoomId());
//        Rooms room = new Rooms();
//        if(roomot.isPresent()) {
//            room = roomot.get();
//        }
//        else{
//            throw new RuntimeException("Không tìm thấy phòng với id = " + request.getRoomId());
//        }
        Contracts contract = resolveContract(request, room);
        System.out.println(contract.getRoom());
        System.out.println(contract.getContractCode());
        UtilityBills previousBill = resolvePreviousBill(room.getId(), request.getBillingMonth());

        double electricityOldIndex = request.getElectricityOldIndex() != null
                ? request.getElectricityOldIndex()
                : previousValue(previousBill != null ? previousBill.getElectricityNewIndex() : null);

        double waterOldIndex = request.getWaterOldIndex() != null
                ? request.getWaterOldIndex()
                : previousValue(previousBill != null ? previousBill.getWaterNewIndex() : null);

        double electricityNewIndex = requiredNumber(request.getElectricityNewIndex(), "electricityNewIndex");
        double waterNewIndex = requiredNumber(request.getWaterNewIndex(), "waterNewIndex");

        if (electricityNewIndex < electricityOldIndex) {
            throw new RuntimeException("Chỉ số điện mới phải lớn hơn hoặc bằng chỉ số điện cũ");
        }

        if (waterNewIndex < waterOldIndex) {
            throw new RuntimeException("Chỉ số nước mới phải lớn hơn hoặc bằng chỉ số nước cũ");
        }

        double electricityConsumed = round(electricityNewIndex - electricityOldIndex);
        double waterConsumed = round(waterNewIndex - waterOldIndex);

        double electricityPrice = request.getElectricityPricePerKwh() != null
                ? request.getElectricityPricePerKwh()
                : safe(room.getElectricityPricePerKwh());

        double waterPrice = request.getWaterPricePerM3() != null
                ? request.getWaterPricePerM3()
                : safe(room.getWaterPricePerM3());

        double electricityAmount = money(electricityConsumed, electricityPrice);
        double waterAmount = money(waterConsumed, waterPrice);

        double internetFee = request.getInternetFee() != null ? request.getInternetFee() : safe(room.getInternetFee());
        double parkingFee = request.getParkingFee() != null ? request.getParkingFee() : safe(room.getParkingFee());
        double cleaningFee = request.getCleaningFee() != null ? request.getCleaningFee() : safe(room.getCleaningFee());
        double otherFee = request.getOtherFee() != null ? request.getOtherFee() : 0.0;

        double serviceAmount = round(internetFee + parkingFee + cleaningFee + otherFee);
        double utilityAmount = round(electricityAmount + waterAmount + serviceAmount);

        double rentAmount = contract != null
                ? round(safe(contract.getMonthlyRent()))
                : round(safe(room.getRentPrice()));

        double totalAmount = round(rentAmount + utilityAmount);
        LocalDate dueDate = resolveDueDate(request, contract);

        CalculationResult result = new CalculationResult();
        result.room = room;
        result.contract = contract;
        result.billingMonth = request.getBillingMonth();
        result.rentAmount = rentAmount;

        result.electricityOldIndex = electricityOldIndex;
        result.electricityNewIndex = electricityNewIndex;
        result.electricityConsumed = electricityConsumed;
        result.electricityPricePerKwh = round(electricityPrice);
        result.electricityAmount = electricityAmount;

        result.waterOldIndex = waterOldIndex;
        result.waterNewIndex = waterNewIndex;
        result.waterConsumed = waterConsumed;
        result.waterPricePerM3 = round(waterPrice);
        result.waterAmount = waterAmount;

        result.internetFee = round(internetFee);
        result.parkingFee = round(parkingFee);
        result.cleaningFee = round(cleaningFee);
        result.otherFee = round(otherFee);
        result.serviceAmount = serviceAmount;
        result.utilityAmount = utilityAmount;
        result.totalAmount = totalAmount;

        result.otherFeeNote = request.getOtherFeeNote();
        result.notes = request.getNotes();
        result.dueDate = dueDate;
        return result;
    }

    private void validateRequest(MonthlyBillRequest request) {
        if (request == null) {
            throw new RuntimeException("Request không được để trống");
        }
        if (request.getRoomId() == null) {
            throw new RuntimeException("roomId là bắt buộc");
        }
        if (request.getBillingMonth() == null || request.getBillingMonth()
                                                        .isBlank()) {
            throw new RuntimeException("billingMonth là bắt buộc, định dạng yyyy-MM");
        }
        parseBillingMonth(request.getBillingMonth());
    }

    private Contracts resolveContract(MonthlyBillRequest request, Rooms room) {
        if (request.getContractId() != null) {
            Contracts contract = contractsRepository.findById(request.getContractId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy hợp đồng với id = " + request.getContractId()));
            // Kiểm tra contract có thuộc room này không (FK giờ nằm ở Rooms.contract_id)
            Contracts roomContract = room.getContract();
            if (roomContract != null && !roomContract.getId().equals(contract.getId())) {
                throw new RuntimeException("contractId không thuộc roomId đã gửi");
            }
            return contract;
        }

        // Fallback: lấy contract gắn với room (Rooms giữ FK contract_id)
        return room.getContract();
    }

    private UtilityBills resolvePreviousBill(Long roomId, String billingMonth) {
        List<UtilityBills> bills = utilityBillsRepository.findByRoom_IdOrderByCreatedAtDesc(roomId);
        for (UtilityBills bill : bills) {
            if (bill.getBillingMonth() != null && !bill.getBillingMonth()
                                                       .equals(billingMonth)) {
                return bill;
            }
        }
        return null;
    }

    private LocalDate resolveDueDate(MonthlyBillRequest request, Contracts contract) {
        if (request.getDueDate() != null) {
            return request.getDueDate();
        }

        YearMonth yearMonth = parseBillingMonth(request.getBillingMonth());

        if (contract != null && contract.getBillingDate() != null) {
            int billingDay = contract.getBillingDate()
                                     .intValue();
            billingDay = Math.max(1, Math.min(billingDay, yearMonth.lengthOfMonth()));
            return yearMonth.atDay(billingDay);
        }

        return yearMonth.atEndOfMonth();
    }

    private YearMonth parseBillingMonth(String billingMonth) {
        try {
            return YearMonth.parse(billingMonth);
        } catch (DateTimeParseException ex) {
            throw new RuntimeException("billingMonth phải có định dạng yyyy-MM");
        }
    }

    private double previousValue(Double value) {
        return value == null ? 0.0 : value;
    }

    private double requiredNumber(Double value, String fieldName) {
        if (value == null) {
            throw new RuntimeException(fieldName + " là bắt buộc");
        }
        return value;
    }

    private double safe(Double value) {
        return value == null ? 0.0 : value;
    }

    private double safe(BigDecimal value) {
        return value == null ? 0.0 : value.doubleValue();
    }

    private double money(double quantity, double unitPrice) {
        return BigDecimal.valueOf(quantity)
                         .multiply(BigDecimal.valueOf(unitPrice))
                         .setScale(2, RoundingMode.HALF_UP)
                         .doubleValue();
    }

    private double round(double value) {
        return BigDecimal.valueOf(value)
                         .setScale(2, RoundingMode.HALF_UP)
                         .doubleValue();
    }

    private Double toMoney(double value) {
        return Double.parseDouble(String.valueOf(value));
    }

    private MonthlyBillResponse buildResponse(
            CalculationResult result,
            UtilityBills utilityBill,
            RentPayments rentPayment,
            String status,
            String message
    ) {
        MonthlyBillResponse response = new MonthlyBillResponse();
        response.setUtilityBillId(utilityBill != null ? utilityBill.getId() : null);
        response.setRentPaymentId(rentPayment != null ? rentPayment.getId() : null);

        response.setRoomId(result.room.getId());
        response.setRoomNumber(result.room.getRoomNumber());

        response.setContractId(result.contract != null ? result.contract.getId() : null);
        response.setContractCode(result.contract != null ? result.contract.getContractCode() : null);

        response.setBillingMonth(result.billingMonth);
        response.setRentAmount(result.rentAmount);

        response.setElectricityOldIndex(result.electricityOldIndex);
        response.setElectricityNewIndex(result.electricityNewIndex);
        response.setElectricityConsumed(result.electricityConsumed);
        response.setElectricityPricePerKwh(result.electricityPricePerKwh);
        response.setElectricityAmount(result.electricityAmount);

        response.setWaterOldIndex(result.waterOldIndex);
        response.setWaterNewIndex(result.waterNewIndex);
        response.setWaterConsumed(result.waterConsumed);
        response.setWaterPricePerM3(result.waterPricePerM3);
        response.setWaterAmount(result.waterAmount);

        response.setInternetFee(result.internetFee);
        response.setParkingFee(result.parkingFee);
        response.setCleaningFee(result.cleaningFee);
        response.setOtherFee(result.otherFee);
        response.setServiceAmount(result.serviceAmount);
        response.setUtilityAmount(result.utilityAmount);
        response.setTotalAmount(result.totalAmount);

        response.setOtherFeeNote(result.otherFeeNote);
        response.setDueDate(result.dueDate);
        response.setStatus(status);
        response.setNotes(result.notes);
        response.setMessage(message);
        return response;
    }

    private static class CalculationResult {
        private Rooms room;
        private Contracts contract;
        private String billingMonth;
        private double rentAmount;

        private double electricityOldIndex;
        private double electricityNewIndex;
        private double electricityConsumed;
        private double electricityPricePerKwh;
        private double electricityAmount;

        private double waterOldIndex;
        private double waterNewIndex;
        private double waterConsumed;
        private double waterPricePerM3;
        private double waterAmount;

        private double internetFee;
        private double parkingFee;
        private double cleaningFee;
        private double otherFee;
        private double serviceAmount;
        private double utilityAmount;
        private double totalAmount;

        private String otherFeeNote;
        private String notes;
        private LocalDate dueDate;
    }

    public SuggestionBillingResponse createSuggestionBillingResponse(Long roomId) {
        Rooms currentRoom = roomsRepository.findRoomsById(roomId)
                                           .orElseThrow(() -> new RuntimeException("Không hiển thị phòng"));
        SuggestionBillingResponse response = SuggestionBillingResponse.builder()
                .rentPrice(currentRoom.getRentPrice())
                .internetFee(currentRoom.getInternetFee())
               .parkingFee(currentRoom.getParkingFee())
               .cleaningFee(currentRoom.getCleaningFee())
               .electricityPricePerKwh(currentRoom.getElectricityPricePerKwh())
               .waterPricePerM3(currentRoom.getWaterPricePerM3())
               .build();
        return response;
    }
}