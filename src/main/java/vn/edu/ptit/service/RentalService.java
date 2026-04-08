package vn.edu.ptit.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import vn.edu.ptit.dto.Request.RentalRequestDTO;
import vn.edu.ptit.dto.Response.ApiResponse;
import vn.edu.ptit.entity.Customer;
import vn.edu.ptit.entity.RentalRequests;
import vn.edu.ptit.entity.RoomPosts;
import vn.edu.ptit.repository.CustomerRepository;
import vn.edu.ptit.repository.RentalRequestRepository;
import vn.edu.ptit.repository.RoomPostRepository;
import vn.edu.ptit.service.Authentication.AuthService;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RentalService {
    private final RentalRequestRepository rentalRequestRepository;
    private final RoomPostRepository roomPostRepository;
    private final CustomerRepository customerRepository;
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
    
}
