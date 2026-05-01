package vn.edu.ptit.service;


import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import vn.edu.ptit.dto.Request.ContractCreationRequest;
import vn.edu.ptit.dto.Response.ApiResponse;
import vn.edu.ptit.dto.Response.ContractResponseDTO;
import vn.edu.ptit.dto.Response.ContractSuggestionResponse;
import vn.edu.ptit.entity.*;
import vn.edu.ptit.repository.*;
import vn.edu.ptit.service.Authentication.AuthService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static vn.edu.ptit.dto.Response.UserResponseDTO.mapToUserResponse;
import static vn.edu.ptit.entity.RentalRequests.Status.PENDING;

@Service
@AllArgsConstructor
public class ContractService {
    private final ContractsRepository contractsRepository;
    private final RoomsRepository roomsRepository;
    private final AuthService authService;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final ContractsRepository contractRepository;

    public ContractSuggestionResponse getContractDraft(Long roomId, Long userId) {
        LandLord currentLandLord = authService.getCurrentLandLord();
        Customer currentCustomer = customerRepository.findCustomerById(userId).orElseThrow(() -> new RuntimeException("Customer not found"));
        Rooms currentRoom = roomsRepository.findRoomsById(roomId).orElseThrow(() -> new RuntimeException("Room not found"));
        ContractSuggestionResponse contractSuggestionResponse = ContractSuggestionResponse.builder()
                .landLordId(currentLandLord.getId())
                .landLordName(currentLandLord.getFullName())
                .landLordIdentityNumber(currentLandLord.getIdCardNumber())
                .landLordAddress(currentLandLord.getAddress())
                .customerId(currentCustomer.getId())
                .customerName(currentCustomer.getFullName())
                .customerIdentityNumber(currentCustomer.getIdCardNumber())
                .customerAddress(currentCustomer.getAddress())
                .roomId(currentRoom.getId())
                .roomAddress(currentRoom.getAddress())
                .roomArea(currentRoom.getAreaM2())
                .rentPrice(currentRoom.getRentPrice())
                .address(currentRoom.getAddress())
                .ward(currentRoom.getWard())
                .district(currentRoom.getDistrict())
                .city(currentRoom.getCity())
                .areaM2(currentRoom.getAreaM2())
                .maxOccupants(currentRoom.getMaxOccupants())
                .electricityPricePerKwh(currentRoom.getElectricityPricePerKwh())
                .waterPricePerM3(currentRoom.getWaterPricePerM3())
                .internetFee(currentRoom.getInternetFee())
                .parkingFee(currentRoom.getParkingFee())
                .cleaningFee(currentRoom.getCleaningFee())
                .build();
        return contractSuggestionResponse;
    }

    public ApiResponse createContract(ContractCreationRequest request) {
        ApiResponse apiResponse = new ApiResponse();
        String randomPart = java.util.UUID.randomUUID().toString()
                                          .substring(0, 8).toUpperCase();
        Contracts contracts = Contracts.builder()
           .contractCode("HD-" + java.time.LocalDate.now().getYear() + "-" + randomPart)
           .startDate(request.getStartDate())
           .endDate(request.getEndDate())
           .monthlyRent(request.getMonthlyRent())
           .depositAmount(request.getDepositAmount())
           .billingDate(request.getBillingDate())
           .status(String.valueOf(RentalRequests.Status.PENDING))
           .contractFileUrl(null)
           .numOccupants(request.getNumOccupants())
           .notes(null)
           .createdAt(LocalDateTime.now())
           .electricityPricePerKwh(request.getElectricityRate())
           .waterPricePerM3(request.getWaterRate())
           .internetFee(request.getInternetFee())
           .parkingFee(request.getParkingFee())
           .cleaningFee(request.getCleaningFee())
           .landLord(userRepository.findById(request.getLandLordId()).orElseThrow(() -> new RuntimeException("Landlord not found")))
           .customer(userRepository.findById(request.getCustomerId()).orElseThrow(() -> new RuntimeException("Customer not found")))
           .room(roomsRepository.findRoomsById(request.getRoomId()).orElseThrow(() -> new RuntimeException("Room not found")))
           .build();
        contractRepository.save(contracts);
        return ApiResponse.builder()
                .success(true)
                .message("Hợp đồng đã được tạo thành công, đang chờ phê duyệt từ chủ nhà")
                .build();
    }

    public List<ContractResponseDTO> getMyContracts(){
        Long userId = authService.getCurrentUser().getId();
        List<Contracts> contracts = contractsRepository.findContractsByUserIdOrderByCreatedAt(userId);
        return contracts.stream().map(contract -> ContractResponseDTO.builder()
                                                                                                   .id(contract.getId())
        .contractCode(contract.getContractCode())
        .startDate(contract.getStartDate())
        .endDate(contract.getEndDate())
        .monthlyRent(contract.getMonthlyRent())
        .depositAmount(contract.getDepositAmount())
        .billingDate(contract.getBillingDate())
        .status(contract.getStatus())
        .electricityPricePerKwh(contract.getElectricityPricePerKwh())
        .waterPricePerM3(contract.getWaterPricePerM3())
        .internetFee(contract.getInternetFee())
        .parkingFee(contract.getParkingFee())
        .cleaningFee(contract.getCleaningFee())
        // Mapping thông tin 2 bên A và B
        .landLord(mapToUserResponse(contract.getLandLord()))
        .customer(mapToUserResponse(contract.getCustomer()))
        // Lấy địa chỉ phòng (null-safe)
        .roomAddress(contract.getRoom() != null ? contract.getRoom().getAddress() : "N/A")
        .build()
).collect(Collectors.toList());

    }
}
