package vn.edu.ptit.service;


import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import vn.edu.ptit.dto.Request.CreateRoomManageRequest;
import vn.edu.ptit.dto.Response.ApiResponse;
import vn.edu.ptit.dto.Response.RoomManageDetailResponse;
import vn.edu.ptit.dto.Response.RoomManageSummaryResponse;
import vn.edu.ptit.entity.Contracts;
import vn.edu.ptit.entity.Rooms;
import vn.edu.ptit.entity.User;
import vn.edu.ptit.repository.ContractsRepository;
import vn.edu.ptit.repository.RoomsRepository;
import vn.edu.ptit.repository.UserRepository;
import vn.edu.ptit.service.Authentication.AuthService;

import java.time.LocalDateTime;
import java.util.List;

import static vn.edu.ptit.entity.Rooms.Status.AVAILABLE;

@Service
@AllArgsConstructor
public class RoomService {
    private final RoomsRepository roomsRepository;
    private final UserRepository userRepository;
    private final AuthService authService;
    private final ContractsRepository contractsRepository;
    public ApiResponse createRoomsManagement(CreateRoomManageRequest request) {
        User landLord = authService.getUser();
        User customer = userRepository.findByEmail(request.getCustomerEmail()).orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng với email: " + request.getCustomerEmail()));
        Contracts contract = contractsRepository.findByContractCode(request.getContractCode()).orElseThrow(() -> new RuntimeException("Không tìm thấy hợp đồng với mã: " + request.getContractCode()));
        if(!contract.getLandLord().getId().equals(landLord.getId())) {
            throw new RuntimeException("Hợp đồng không thuộc về chủ nhà hiện tại");
        }
        if(!contract.getCustomer().getId().equals(customer.getId())) {
            throw new RuntimeException("Hợp đồng không thuộc về khách hàng được chỉ định");
        }
        Rooms rooms = Rooms.builder()
                   .roomNumber(request.getRoomNumber())
                   .address(request.getAddress())
                   .ward(request.getWard())
                   .district(request.getDistrict())
                   .city(request.getCity())
                   .areaM2(request.getAreaM2())
                   .maxOccupants(request.getMaxOccupants())
                   .rentPrice(request.getRentPrice())
                   .electricityPricePerKwh(request.getElectricityPricePerKwh())
                   .waterPricePerM3(request.getWaterPricePerM3())
                   .internetFee(request.getInternetFee())
                   .parkingFee(request.getParkingFee())
                   .cleaningFee(request.getCleaningFee())
                   .status(AVAILABLE)
                   .roomType(request.getRoomType())
                   .description(request.getDescription())
                   .createdAt(LocalDateTime.now())
                   .updatedAt(LocalDateTime.now())
                   .landLord(landLord)
                   .customer(customer)
                   .contract(contract)
                   .utilityBills(null)
                   .rentPayments(null)
                   .chatRoom(null)
                   .notifications(null)
                   .build();
        roomsRepository.save(rooms);
        return ApiResponse.builder()
                .success(true)
                .message("Tạo phòng để quản lý thành công")
                .build();
    }

    public List<RoomManageSummaryResponse> getRoomsManagementSummary() {
        User user = authService.getUser();
        List<Rooms> rooms = roomsRepository.findRoomsByLandLordId(user.getId());
        return rooms.stream().map(room -> RoomManageSummaryResponse.builder()
                .id(room.getId())
                .roomNumber(room.getRoomNumber())
                .roomType(room.getRoomType())
                .rentPrice(room.getRentPrice())
                .status(room.getStatus().name())
                .userEmail(room.getCustomer() != null ? room.getCustomer().getEmail() : null)
                .userName(room.getCustomer() != null ? room.getCustomer().getFullName() : null)
                .build()).toList();
    }
    public RoomManageSummaryResponse getRoomDetailManagement(Long id) {
        return roomsRepository.findRoomsById(id).stream().map(room -> RoomManageSummaryResponse.builder()
                .id(room.getId())
                .roomNumber(room.getRoomNumber())
                .roomType(room.getRoomType())
                .rentPrice(room.getRentPrice())
                .status(room.getStatus().name())
                .userEmail(room.getCustomer() != null ? room.getCustomer().getEmail() : null)
                .userName(room.getCustomer() != null ? room.getCustomer().getFullName() : null)
                .build()).findFirst().orElseThrow(() -> new RuntimeException("Không tìm thấy phòng với id: " + id));
    }

}
