package vn.edu.ptit.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import vn.edu.ptit.dto.Request.RentalRequestDTO;
import vn.edu.ptit.dto.Response.ApiResponse;
import vn.edu.ptit.dto.Response.RentalRequestResponse;
import vn.edu.ptit.entity.Customer;
import vn.edu.ptit.entity.LandLord;
import vn.edu.ptit.entity.RentalRequests;
import vn.edu.ptit.entity.RoomPosts;
import vn.edu.ptit.repository.RentalRequestRepository;
import vn.edu.ptit.repository.RoomPostRepository;
import vn.edu.ptit.service.Authentication.AuthService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RentalRequestService {
    private final RentalRequestRepository rentalRequestRepository;
    private final RoomPostRepository roomPostRepository;
    private final AuthService authService;

    public ApiResponse createNewRentalRequest(RentalRequestDTO request) {
        RentalRequests rentalRequests = new RentalRequests();
        BeanUtils.copyProperties(request, rentalRequests);
        RoomPosts roomPost = roomPostRepository.findById(request.getRoomPostId()).orElseThrow();
        rentalRequests.setRoomPost(roomPost);
        rentalRequests.setLandlord(roomPost.getLandlord());
        System.out.println(authService.getCurrentUserId());
        Customer customerRef = new Customer();
        customerRef.setId(authService.getCurrentUserId());
        rentalRequests.setCustomer(customerRef);
        rentalRequests.setCreatedAt(LocalDateTime.now());
        rentalRequests.setStatus(RentalRequests.Status.PENDING);
        rentalRequestRepository.save(rentalRequests);
        return ApiResponse.builder().message("Tạo yêu cầu thuê phòng thành công").success(true).build();
    }

    public List<RentalRequestResponse> findRentalRequestByLandLordId() {
        LandLord landLord = authService.getCurrentLandLord();
        List<Object[]> rentalRequestsList = rentalRequestRepository
                .findAllWithRoomPostAndCustomerByLandLordIdOrderByCreatedAt(landLord.getId());
        return rentalRequestsList.stream().map(row -> {
            RentalRequests rentalRequests = (RentalRequests) row[0];
            String mainImageUrl = (String) row[1];
            Long customerId = (Long) row[2];
            return RentalRequestResponse.builder()
                    .id(rentalRequests.getId())
                    .status(rentalRequests.getStatus())
                    .createdAt(rentalRequests.getCreatedAt())
                    .roomPost(RentalRequestResponse.RoomPostInfo.builder()
                            .id(rentalRequests.getRoomPost().getId())
                            .title(rentalRequests.getRoomPost().getTitle())
                            .thumbnailUrl(mainImageUrl)
                            .build())
                    .customer(RentalRequestResponse.UserInfo.builder()
                            .id(customerId)
                            .fullName(rentalRequests.getCustomer().getFullName())
                            .avatarUrl(rentalRequests.getCustomer().getAvatarUrl())
                            .build())
                    .landlord(RentalRequestResponse.UserInfo.builder()
                            .id(landLord.getId())
                            .fullName(landLord.getFullName())
                            .avatarUrl(landLord.getAvatarUrl())
                            .build())
                    // contractId sẽ được set sau khi tạo hợp đồng
                    .build();
        }).collect(Collectors.toList());
    }

    public ApiResponse updateRequestStatus(Long requestId, RentalRequests.Status newStatus) {
        if (newStatus == null) {
            return ApiResponse.builder()
                    .success(false)
                    .message("Trạng thái không hợp lệ")
                    .build();
        }

        if (newStatus != RentalRequests.Status.APPROVED && newStatus != RentalRequests.Status.REJECTED) {
            return ApiResponse.builder()
                    .success(false)
                    .message("Chỉ cho phép chuyển sang APPROVED hoặc REJECTED")
                    .build();
        }

        LandLord landLord = authService.getCurrentLandLord();
        RentalRequests request = rentalRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy yêu cầu"));

        if (!request.getLandlord().getId().equals(landLord.getId())) {
            throw new RuntimeException("Bạn không có quyền xử lý yêu cầu này");
        }

        if (request.getStatus() != RentalRequests.Status.PENDING) {
            return ApiResponse.builder()
                    .success(false)
                    .message("Yêu cầu đã được xử lý trước đó")
                    .build();
        }

        request.setStatus(newStatus);
        request.setReviewedAt(LocalDateTime.now());
        rentalRequestRepository.save(request);

        return ApiResponse.builder()
                .success(true)
                .message("Cập nhật trạng thái thành công")
                .build();
    }

}
