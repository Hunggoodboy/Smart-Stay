package vn.edu.ptit.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import vn.edu.ptit.dto.Request.CreateRentalRequestRequest;
import vn.edu.ptit.dto.Request.CreateRoomManageRequest;
import vn.edu.ptit.dto.Request.RentalRequestDTO;
import vn.edu.ptit.dto.Response.ApiResponse;
import vn.edu.ptit.dto.Response.RentalRequestResponse;
import vn.edu.ptit.entity.*;
import vn.edu.ptit.repository.*;
import vn.edu.ptit.service.Authentication.AuthService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RentalRequestService {
    private final RentalRequestRepository rentalRequestRepository;
    private final RoomPostRepository roomPostRepository;
    private final RoomsRepository roomsRepository;
    private final AuthService authService;
    private final ContractsRepository  contractsRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    public ApiResponse createNewRentalRequest(RentalRequestDTO request) {
        Long currentId = authService.getCurrentUser().getId();

        // Kiểm tra đã gửi yêu cầu chưa
        if (rentalRequestRepository.existsByRoomPostIdAndCustomerId(request.getRoomPostId(), currentId)) {
            throw new RuntimeException("Bạn đã yêu cầu thuê phòng này, yêu cầu chờ chủ nhà xác nhận");
        }

        // Kiểm tra phòng đã có người đang thuê chưa
        if (roomsRepository.existsByRoomPostIdAndStatus(request.getRoomPostId(), Rooms.Status.RENTED)) {
            throw new RuntimeException("Phòng này hiện đã có người thuê, vui lòng chọn phòng khác");
        }

        Optional<Customer> currentCustomer = customerRepository.findById(currentId);
        if(!userRepository.findById(currentId).isPresent()  && currentCustomer.isEmpty()){
            return ApiResponse.builder()
                              .success(false)
                              .message("REQUIRE_USER_INFO") // Tín hiệu đặc biệt
                              .build();
        }
        else if(currentCustomer.isEmpty()){
            return ApiResponse.builder()
                              .success(false)
                              .message("REQUIRE_CUSTOMER_INFO") // Tín hiệu đặc biệt
                              .build();
        }
        RentalRequests rentalRequests = new RentalRequests();
        BeanUtils.copyProperties(request, rentalRequests);
        RoomPosts roomPost = roomPostRepository.findById(request.getRoomPostId()).orElseThrow();
        rentalRequests.setRoomPost(roomPost);
        rentalRequests.setLandlord(roomPost.getLandlord());
        Customer customerRef = currentCustomer.get();
        rentalRequests.setCustomer(customerRef);
        rentalRequests.setCreatedAt(LocalDateTime.now());
        rentalRequests.setStatus(RentalRequests.Status.PENDING);
        rentalRequestRepository.save(rentalRequests);
        return ApiResponse.builder()
                          .message("Tạo yêu cầu thuê phòng thành công")
                          .success(true)
                          .build();
    }

    public CreateRoomManageRequest getRecommendedRentalRequest(Long requestId) {
        RentalRequests rentalRequests = rentalRequestRepository.findById(requestId)
                                                               .orElseThrow(() -> new RuntimeException("Phòng này chưa được gửi yêu cầu thuê"));
        RoomPosts roomPost = rentalRequests.getRoomPost();
        Customer customerRef = rentalRequests.getCustomer();
        LandLord landlordRef = rentalRequests.getLandlord();
        Contracts contractRef = contractsRepository.findByCustomerIdAndLandLordId(customerRef.getId(), landlordRef.getId())
                                                   .orElseThrow(() -> new RuntimeException("Bạn chưa làm hợp đồng, vui lòng tiến hành làm hợp đồng trước"));
        return CreateRoomManageRequest.builder()
                    .roomNumber(null)
                    .roomType(roomPost.getRoomType())
                    .description(roomPost.getDescription())
                    .contractId(contractRef.getId())
                    .address(roomPost.getAddress())
                    .ward(roomPost.getWard())
                    .district(roomPost.getDistrict())
                    .city(roomPost.getCity())
                    .areaM2(roomPost.getAreaM2())
                    .maxOccupants(Long.valueOf(roomPost.getMaxOccupants()))
                    .rentPrice(roomPost.getMonthlyRent().toBigInteger()
                                       .doubleValue())
                    .electricityPricePerKwh(roomPost.getElectricityPricePerKwh().toBigInteger()
                                                    .doubleValue())
                    .waterPricePerM3(roomPost.getWaterPricePerM3().toBigInteger()
                                             .doubleValue())
                    .internetFee(roomPost.getInternetFee())
                    .parkingFee(roomPost.getParkingFee())
                    .cleaningFee(roomPost.getCleaningFee())
                    .build();
    }

    public List<RentalRequestResponse> findRentalRequestByLandLordId() {
        LandLord landLord = authService.getCurrentLandLord();
        List<Object[]> rentalRequestsList = rentalRequestRepository.findAllWithRoomPostAndCustomer(landLord.getId());
        return mapToResponse(rentalRequestsList);
    }

    /**
     * Lấy tất cả yêu cầu thuê liên quan đến người dùng hiện tại (cả Landlord lẫn Customer).
     * Repository query đã filter: (landlord.id = userId OR customer.id = userId)
     */
    public List<RentalRequestResponse> findMyRequests() {
        Long currentUserId = authService.getCurrentUserId();
        List<Object[]> rentalRequestsList = rentalRequestRepository.findAllWithRoomPostAndCustomer(currentUserId);
        return mapToResponse(rentalRequestsList);
    }

    private List<RentalRequestResponse> mapToResponse(List<Object[]> rentalRequestsList) {
        return rentalRequestsList.stream()
                                 .map(row -> {
                                     RentalRequests rentalRequests = (RentalRequests) row[0];
                                     String mainImageUrl = (String) row[1];
                                     Long customerId = (Long) row[2];
                                     Contracts contract = contractsRepository.findByRentalRequestId(rentalRequests.getId()).orElse(null);
                                     Long contractId = null;
                                     if(contract != null) {
                                         contractId = contract.getId();
                                     }
                                     return RentalRequestResponse.builder()
                                                                 .id(rentalRequests.getId())
                                                                 .status(rentalRequests.getStatus())
                                                                 .createdAt(rentalRequests.getCreatedAt())
                                                                 .contractId(contractId)
                                                                 .roomPost(RentalRequestResponse.RoomPostInfo.builder()
                                                                                                             .id(rentalRequests.getRoomPost()
                                                                                                                               .getId())
                                                                                                             .title(rentalRequests.getRoomPost()
                                                                                                                                  .getTitle())
                                                                                                             .thumbnailUrl(mainImageUrl)
                                                                                                             .build())
                                                                 .customer(RentalRequestResponse.UserInfo.builder()
                                                                                                         .id(customerId)
                                                                                                         .fullName(rentalRequests.getCustomer()
                                                                                                                                 .getFullName())
                                                                                                         .avatarUrl(rentalRequests.getCustomer()
                                                                                                                                  .getAvatarUrl())
                                                                                                         .build())
                                                                 .landlord(RentalRequestResponse.UserInfo.builder()
                                                                                                         .id(rentalRequests.getLandlord()
                                                                                                                           .getId())
                                                                                                         .fullName(rentalRequests.getLandlord()
                                                                                                                                 .getFullName())
                                                                                                         .avatarUrl(rentalRequests.getLandlord()
                                                                                                                                  .getAvatarUrl())
                                                                                                         .build())
                                                                 .build();
                                 })
                                 .collect(Collectors.toList());
    }

    public ApiResponse changeRequestStatus(Long requestId, String status) {
        RentalRequests rentalRequests = rentalRequestRepository.findById(requestId)
                                                               .orElseThrow();
        try {
            RentalRequests.Status newStatus = RentalRequests.Status.valueOf(status.toUpperCase());
            rentalRequests.setStatus(newStatus);
            rentalRequestRepository.save(rentalRequests);
            return ApiResponse.builder()
                              .message("Cập nhật trạng thái yêu cầu thành công")
                              .success(true)
                              .build();
        } catch (IllegalArgumentException e) {
            return ApiResponse.builder()
                              .message("Trạng thái không hợp lệ")
                              .success(false)
                              .build();
        }
    }

    public ApiResponse deleteRequest(Long requestId) {
        try {
            rentalRequestRepository.deleteById(requestId);
            return ApiResponse.builder()
                              .success(true)
                              .message("Bạn đã xoá yêu cầu thuê phòng này thành công")
                              .build();
        } catch (Exception e) {
            throw new RuntimeException("Yêu cầu chưa được thực hiện");
        }
    }
}
