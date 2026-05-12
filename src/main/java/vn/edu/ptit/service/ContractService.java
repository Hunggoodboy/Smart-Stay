package vn.edu.ptit.service;


import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    private final RentalRequestRepository rentalRequestRepository;
    private final RoomPostRepository roomPostRepository;
    private final AuthService authService;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final ContractsRepository contractRepository;

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void updateContracts() {
        contractRepository.updateExpiredContracts();
    }

    public ContractSuggestionResponse getContractDraft(Long roomPostId, Long userId) {
        LandLord currentLandLord = authService.getCurrentLandLord();
        Customer currentCustomer = customerRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));

        // Lấy thông tin từ RoomPost (chưa có Rooms ở giai đoạn tạo hợp đồng)
        RoomPosts roomPost = roomPostRepository.findById(roomPostId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài đăng phòng"));

        return ContractSuggestionResponse.builder()
                .landLordId(currentLandLord.getId())
                .landLordName(currentLandLord.getFullName())
                .landLordIdentityNumber(currentLandLord.getIdCardNumber())
                .landLordAddress(currentLandLord.getAddress())
                .customerId(currentCustomer.getId())
                .customerName(currentCustomer.getFullName())
                .customerAddress(currentCustomer.getAddress())
                .roomPostId(roomPost.getId())          // đổi từ roomId sang roomPostId
                .roomAddress(roomPost.getAddress())
                .roomArea(roomPost.getAreaM2())
                .rentPrice(roomPost.getMonthlyRent() != null ? roomPost.getMonthlyRent().doubleValue() : null)
                .address(roomPost.getAddress())
                .ward(roomPost.getWard())
                .district(roomPost.getDistrict())
                .city(roomPost.getCity())
                .areaM2(roomPost.getAreaM2())
                .maxOccupants(roomPost.getMaxOccupants() != null ? Long.valueOf(roomPost.getMaxOccupants()) : null)
                .electricityPricePerKwh(roomPost.getElectricityPricePerKwh() != null ? roomPost.getElectricityPricePerKwh().doubleValue() : null)
                .waterPricePerM3(roomPost.getWaterPricePerM3() != null ? roomPost.getWaterPricePerM3().doubleValue() : null)
                .internetFee(roomPost.getInternetFee())
                .parkingFee(roomPost.getParkingFee())
                .cleaningFee(roomPost.getCleaningFee())
                .build();
    }

    public ApiResponse createContract(ContractCreationRequest request) {
        String randomPart = java.util.UUID.randomUUID().toString()
                .substring(0, 8).toUpperCase();
        RentalRequests rentalRequests = rentalRequestRepository.findByRoomPostIdAndCustomerId(request.getRoomPostId(), request.getCustomerId()).orElseThrow(
                () -> new RuntimeException("Bạn chưa yêu cầu thuê phòng này")
        );
        Contracts contracts = Contracts.builder()
                .contractCode("HD-" + java.time.LocalDate.now().getYear() + "-" + randomPart)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .monthlyRent(request.getMonthlyRent())
                .depositAmount(request.getDepositAmount())
                .billingDate(request.getBillingDate())
                .status(String.valueOf(RentalRequests.Status.PENDING))
                .rentalRequest(rentalRequests)
                .contractFileUrl(null)
                .numOccupants(request.getNumOccupants())
                .isExpiration(true)
                .notes(null)
                .createdAt(LocalDateTime.now())
                .electricityPricePerKwh(request.getElectricityRate())
                .waterPricePerM3(request.getWaterRate())
                .internetFee(request.getInternetFee())
                .parkingFee(request.getParkingFee())
                .cleaningFee(request.getCleaningFee())
                .landLord(userRepository.findById(request.getLandLordId())
                        .orElseThrow(() -> new RuntimeException("Landlord not found")))
                .customer(userRepository.findById(request.getCustomerId())
                        .orElseThrow(() -> new RuntimeException("Customer not found")))
                // KHAI BÁO: Không gán .room() vì Contracts không còn giữ FK room_id nữa
                // Room sẽ được tạo sau khi Contract được dúyệt và gắn FK contract_id → contracts.id
                .build();
        contractRepository.save(contracts);
        return ApiResponse.builder()
                .success(true)
                .message("Hợp đồng đã được tạo thành công, đang chờ phê duyệt từ chủ nhà")
                .build();
    }

    public List<ContractResponseDTO> getMyValidContracts(){
        Long userId = authService.getCurrentUser().getId();
        List<Contracts> contracts = contractsRepository.findValidContractsForUser(userId);
        System.out.println(userId);
        System.out.println("Số hợp đồng của người dùng là "+contracts.size());
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

    public List<ContractResponseDTO> getMyContractsExpired(){
        Long userId = authService.getCurrentUser().getId();
        List<Contracts> contracts = contractsRepository.findExpiredContractsForUser(userId);
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
