package vn.edu.ptit.service.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.ptit.dto.Response.AdminDashboardCountResponse;
import vn.edu.ptit.entity.RoomPosts;
import vn.edu.ptit.entity.User;
import vn.edu.ptit.repository.AdminDashboardRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final AdminDashboardRepository adminDashboardRepository;

    @Transactional(readOnly = true)
    public AdminDashboardCountResponse getTotalUsers() {
        return countResponse("totalUsers", adminDashboardRepository.countUsers());
    }

    @Transactional(readOnly = true)
    public AdminDashboardCountResponse getTotalCustomers() {
        return countResponse("totalCustomers", adminDashboardRepository.countUsersByRole(User.Role.CUSTOMER));
    }

    @Transactional(readOnly = true)
    public AdminDashboardCountResponse getTotalLandlords() {
        return countResponse("totalLandlords", adminDashboardRepository.countUsersByRole(User.Role.LANDLORD));
    }

    @Transactional(readOnly = true)
    public AdminDashboardCountResponse getPendingLandlordVerifications() {
        return countResponse("pendingLandlordVerifications", adminDashboardRepository.countPendingLandlords());
    }

    @Transactional(readOnly = true)
    public AdminDashboardCountResponse getVerifiedLandlords() {
        return countResponse("verifiedLandlords", adminDashboardRepository.countVerifiedLandlords());
    }

    @Transactional(readOnly = true)
    public AdminDashboardCountResponse getPostsNeedReview() {
        return countResponse("postsNeedReview", adminDashboardRepository.countPostsNeedReview(
                List.of(RoomPosts.Status.DRAFT)
        ));
    }

    @Transactional(readOnly = true)
    public AdminDashboardCountResponse getTotalRoomPosts() {
        return countResponse("totalRoomPosts", adminDashboardRepository.countRoomPosts());
    }

    @Transactional(readOnly = true)
    public AdminDashboardCountResponse getTotalRooms() {
        return countResponse("totalRooms", adminDashboardRepository.countRooms());
    }

    private AdminDashboardCountResponse countResponse(String key, long value) {
        return new AdminDashboardCountResponse(key, value);
    }
}
