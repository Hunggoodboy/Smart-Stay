package vn.edu.ptit.service;


import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import vn.edu.ptit.dto.Request.CreateRoomManageRequest;
import vn.edu.ptit.dto.Response.ApiResponse;
import vn.edu.ptit.dto.Response.RoomManageDetailResponse;
import vn.edu.ptit.dto.Response.RoomManageSummaryResponse;
import vn.edu.ptit.entity.Contracts;
import vn.edu.ptit.entity.RoomPosts;
import vn.edu.ptit.entity.Rooms;
import vn.edu.ptit.entity.User;
import vn.edu.ptit.repository.ContractsRepository;
import vn.edu.ptit.repository.RoomsRepository;
import vn.edu.ptit.repository.UserRepository;
import vn.edu.ptit.repository.RoomPostRepository;
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
    private final RoomPostRepository roomPostRepository;
    public ApiResponse createRoomsManagement(CreateRoomManageRequest request) {
        User landLord = authService.getUser();

        // Tìm Contract theo ID (gửi từ frontend sau khi tạo hợp đồng xong)
        Contracts contract = contractsRepository.findById(request.getContractId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hợp đồng với id: " + request.getContractId()));

        // Kiểm tra hợp đồng có thuộc về chủ nhà hiện tại không
        if (!contract.getLandLord().getId().equals(landLord.getId())) {
            throw new RuntimeException("Ấp đồng này không thuộc về chủ nhà hiện tại");
        }

        // Kiểm tra phòng đã được tạo chưa
        if (roomsRepository.findByContractId(contract.getId()).isPresent()) {
            throw new RuntimeException("Đã có phòng quản lý cho hợp đồng này");
        }

        // Customer lấy từ hợp đồng, không cần truyền thê m email
        User customer = contract.getCustomer();

        // RoomPost gốc (optional — gán để track "bài đăng nào sinh ra phòng này")
        RoomPosts roomPostRef = null;
        if (request.getRoomPostId() != null) {
            roomPostRef = roomPostRepository.findById(request.getRoomPostId()).orElse(null);
        }

        Rooms rooms = Rooms.builder()
                .roomNumber(request.getRoomNumber())
                .address(request.getAddress())
                .ward(request.getWard())
                .district(request.getDistrict())
                .city(request.getCity())
                .areaM2(request.getAreaM2())
                .maxOccupants(request.getMaxOccupants())
                .rentPrice(request.getRentPrice() != null ? request.getRentPrice() : contract.getMonthlyRent())
                .electricityPricePerKwh(request.getElectricityPricePerKwh() != null ? request.getElectricityPricePerKwh() : contract.getElectricityPricePerKwh())
                .waterPricePerM3(request.getWaterPricePerM3() != null ? request.getWaterPricePerM3() : contract.getWaterPricePerM3())
                .internetFee(request.getInternetFee() != null ? request.getInternetFee() : contract.getInternetFee())
                .parkingFee(request.getParkingFee() != null ? request.getParkingFee() : contract.getParkingFee())
                .cleaningFee(request.getCleaningFee() != null ? request.getCleaningFee() : contract.getCleaningFee())
                .status(Rooms.Status.RENTED) // Khi tạo từ Contract → đương nhiên đã cho thuê
                .roomType(request.getRoomType())
                .description(request.getDescription())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .landLord(landLord)
                .customer(customer)
                .contract(contract)  // FK contract_id → contracts.id
                .roomPost(roomPostRef) // FK room_post_id → room_posts.id
                .build();
        roomsRepository.save(rooms);
        return ApiResponse.builder()
                .success(true)
                .message("Tạo phòng quản lý thành công")
                .build();
    }

    public List<RoomManageSummaryResponse> getRoomsManagementSummary() {
        User user = authService.getUser();
        List<Rooms> rooms = roomsRepository.findRoomsByLandLordIdAndDeletedAt(user.getId(), null);
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
    public List<RoomManageSummaryResponse> getRoomsManagementSummaryIsDeleted() {
        User user = authService.getUser();
        List<Rooms> rooms = roomsRepository.findRoomsIsDeletedByLandLordId(user.getId());
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
        Rooms room = roomsRepository.findRoomsById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng với id: " + id));
        // Contract lấy trực tiếp từ Room (Rooms giữ FK contract_id)
        Contracts currentContract = room.getContract();
        return RoomManageSummaryResponse.builder()
                .id(room.getId())
                .roomNumber(room.getRoomNumber())
                .roomType(room.getRoomType())
                .rentPrice(room.getRentPrice())
                .status(room.getStatus().name())
                .address(room.getAddress())
                .contractId(currentContract != null ? currentContract.getId() : null)
                .ward(room.getWard())
                .district(room.getDistrict())
                .city(room.getCity())
                .tenantId(room.getCustomer() != null ? room.getCustomer().getId() : null)
                .userEmail(room.getCustomer() != null ? room.getCustomer().getEmail() : null)
                .userName(room.getCustomer() != null ? room.getCustomer().getFullName() : null)
                .build();
    }

}
