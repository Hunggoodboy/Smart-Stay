package vn.edu.ptit.service;


import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import vn.edu.ptit.dto.Request.UtilityBillsRequest;
import vn.edu.ptit.dto.Response.ApiResponse;
import vn.edu.ptit.dto.Response.UtilityBillsResponse;
import vn.edu.ptit.dto.UserDTO;
import vn.edu.ptit.entity.Contracts;
import vn.edu.ptit.entity.Customer;
import vn.edu.ptit.entity.RentPayments;
import vn.edu.ptit.entity.UtilityBills;
import vn.edu.ptit.repository.*;
import vn.edu.ptit.service.Authentication.AuthService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class PaymentService {
    private final AuthService authService;
    private final UtilityBillsRepository utilityBillsRepository;
    private final ContractsRepository contractsRepository;
    private final RoomsRepository roomsRepository;
    private final LandLordRepository landLordRepository;
    private final RentPaymentsRepository rentPaymentsRepository;
    private final CustomerRepository customerRepository;

    public List<UtilityBillsResponse> getBillByCurrentUser(Authentication authentication) {
        UserDTO userDTO = authService.getCurrentUser();
        List<UtilityBills> utilityBills = utilityBillsRepository.findAllByUserId(userDTO.getId());
        List<UtilityBillsResponse> utilityBillsResponses = utilityBills.stream()
                .map(UtilityBills -> new UtilityBillsResponse().fromEntity(UtilityBills))
                .toList();
        return utilityBillsResponses;
    }

    public ApiResponse setBillForCurrentUser(UtilityBillsRequest request) {
        System.out.println("Room ID nhận được từ API: " + request.getRoomId());
        Contracts contract = contractsRepository.findContractsByRoomId(request.getRoomId()).orElseThrow(() -> new RuntimeException("Phòng này chưa có hợp đồng"));
        UtilityBills utilityBills = new UtilityBills();
        BeanUtils.copyProperties(request, utilityBills);
        utilityBills.setContract(contract);
        utilityBills.setRoom(roomsRepository.findRoomsById(request.getRoomId()).orElseThrow());
        utilityBills.setLandLord(landLordRepository.findLandLordById(authService.getCurrentUserId()).orElseThrow());
        utilityBills.setElectricityOldIndex(request.getElectricityOldIndex());
        utilityBills.setElectricityNewIndex(request.getElectricityNewIndex());
        utilityBills.setElectricityConsumed(request.getElectricityNewIndex() - request.getElectricityOldIndex());
        utilityBills.setElectricityAmount(contract.getElectricityPricePerKwh() * utilityBills.getElectricityConsumed());
        utilityBills.setWaterOldIndex(request.getWaterOldIndex());
        utilityBills.setWaterNewIndex(request.getWaterNewIndex());
        utilityBills.setWaterConsumed(request.getWaterNewIndex() - request.getWaterOldIndex());
        utilityBills.setWaterAmount(contract.getWaterPricePerM3() * utilityBills.getWaterConsumed());
        utilityBills.setParkingFee(contract.getParkingFee());
        utilityBills.setCleaningFee(contract.getCleaningFee());
        utilityBills.setInternetFee(contract.getInternetFee());
        utilityBills.setBillingMonth(request.getBillingMonth());
        utilityBills.setCreatedAt(LocalDateTime.now());
        utilityBills.setTotalAmount(utilityBills.getElectricityAmount() + utilityBills.getWaterAmount() +  utilityBills.getCleaningFee() +  utilityBills.getInternetFee() + utilityBills.getParkingFee());
        utilityBillsRepository.save(utilityBills);
        setRentalPaymentForUser(utilityBills);
        return ApiResponse.builder()
                .success(true)
                .message("Bạn đã thêm hoá đơn thành công")
                .build();
    }

    public void setRentalPaymentForUser(UtilityBills utilityBills) {
        RentPayments rentPayments = new RentPayments();

        // Lấy thông tin Hợp đồng từ Hóa đơn điện nước
        Contracts contract = utilityBills.getContract();

        // 1. Set các mối quan hệ (Foreign Keys)
        rentPayments.setContract(contract);
        rentPayments.setRoom(utilityBills.getRoom());
        Customer customer = customerRepository.findCustomerById(contract.getCustomer().getId()).orElseThrow(() -> new RuntimeException("Không tìm thấy khách thuê"));
        rentPayments.setCustomer(customer); // Khách thuê lấy từ hợp đồng
        rentPayments.setUtilityBill(utilityBills);

        // 2. Set thông tin kỳ thanh toán
        rentPayments.setBillingMonth(utilityBills.getBillingMonth());
        rentPayments.setStatus(RentPayments.Status.UNPAID);
        rentPayments.setCreatedAt(LocalDateTime.now());

        // Hạn nộp: Cài mặc định là 5 ngày sau khi xuất hóa đơn (hoặc tùy logic của bạn)
        rentPayments.setDueDate(LocalDate.now().plusDays(5));

        Double rentAmount = contract.getMonthlyRent();
        rentPayments.setRentAmount(rentAmount != null ? rentAmount : 0.0);

        Double utilityAmount = utilityBills.getTotalAmount();
        rentPayments.setUtilityAmount(utilityAmount != null ? utilityAmount : 0.0);

        rentPayments.setLateFee(0.0);

        Double totalAmount = rentPayments.getRentAmount() + rentPayments.getUtilityAmount() + rentPayments.getLateFee();
        rentPayments.setTotalAmount(totalAmount);

        // 4. Lưu vào Database
        rentPaymentsRepository.save(rentPayments);
    }
}