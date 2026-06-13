package vn.edu.ptit.service.admin;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import vn.edu.ptit.dto.Response.AdminSystemStatsResponse;
import vn.edu.ptit.entity.Customer;
import vn.edu.ptit.entity.LandLord;
import vn.edu.ptit.entity.RentPayments;
import vn.edu.ptit.entity.RoomPosts;
import vn.edu.ptit.entity.User;
import vn.edu.ptit.repository.AdminDashboardRepository;
import vn.edu.ptit.repository.AdminSystemStatsRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class AdminSystemStatsService {

    private final AdminDashboardRepository adminDashboardRepository;
    private final AdminSystemStatsRepository adminSystemStatsRepository;

    public AdminSystemStatsResponse getSystemStats(int year) {
        // 4 card tổng quan — dùng lại repo có sẵn
        long totalUsers = adminDashboardRepository.countUsers();
        long totalRooms = adminDashboardRepository.countRooms();
        long pendingApprovals = adminDashboardRepository.countPendingLandlords();
        long postsNeedReview = adminDashboardRepository.countPostsNeedReview(
                List.of(RoomPosts.Status.DRAFT, RoomPosts.Status.INACTIVE)
        );

        // Phân bố người dùng
        long customerCount = adminDashboardRepository.countUsersByRole(User.Role.CUSTOMER);
        long landlordCount = adminDashboardRepository.countUsersByRole(User.Role.LANDLORD);
        long adminCount = adminDashboardRepository.countUsersByRole(User.Role.ADMIN);
        //tổng số lượng chủ nhà đã được hệ thống phê duyệt/xác minh thành công
        long verifiedLandlords = adminDashboardRepository.countVerifiedLandlords();
        long totalLandlords = landlordCount;

        // Hoạt động theo tháng
        List<AdminSystemStatsResponse.MonthlyActivity> monthlyStats = buildMonthlyStats(year);

        // Người dùng gần đây (top 10)
        List<User> recentUserEntities = adminSystemStatsRepository.findRecentUsers();
        List<AdminSystemStatsResponse.RecentUser> recentUsers = recentUserEntities.stream()
                .limit(10)
                .map(this::mapToRecentUser)
                .collect(Collectors.toList());

        return AdminSystemStatsResponse.builder()
                .totalUsers(totalUsers)
                .totalRooms(totalRooms)
                .pendingApprovals(pendingApprovals)
                .postsNeedReview(postsNeedReview)
                .customerCount(customerCount)
                .landlordCount(landlordCount)
                .adminCount(adminCount)
                .verifiedLandlords(verifiedLandlords)
                .totalLandlords(totalLandlords)
                .monthlyStats(monthlyStats)
                .recentUsers(recentUsers)
                .build();
    }

    private List<AdminSystemStatsResponse.MonthlyActivity> buildMonthlyStats(int year) {
        // Query từng loại dữ liệu
        //dem so tai khoan đc tao trong tháng
        Map<Integer, Long> registrations = toMonthMap(adminSystemStatsRepository.countNewUsersByMonth(year));
        //đếm số phòng được thuê trong tháng
        Map<Integer, Long> rentals = toMonthMap(adminSystemStatsRepository.countRoomsRentedByMonth(year));

        String yearText = String.valueOf(year);
        Map<Integer, Double> revenues = new HashMap<>();
        List<Object[]> revenueData = adminSystemStatsRepository.revenueByMonth(yearText, RentPayments.Status.PAID);
        for (Object[] row : revenueData) {
            int month = Integer.parseInt((String) row[0]);
            double amount = ((Number) row[1]).doubleValue();
            revenues.put(month, amount);
        }

        // Ghép thành 12 tháng
        int currentMonth = LocalDateTime.now().getYear() == year
                ? LocalDateTime.now().getMonthValue()
                : 12;

        List<AdminSystemStatsResponse.MonthlyActivity> result = new ArrayList<>();
        for (int m = 1; m <= currentMonth; m++) {
            result.add(AdminSystemStatsResponse.MonthlyActivity.builder()
                    .month(m)
                    .newRegistrations(registrations.getOrDefault(m, 0L))
                    .roomsRented(rentals.getOrDefault(m, 0L))
                    .revenue(revenues.getOrDefault(m, 0.0))
                    .build());
        }
        return result;
    }

    private Map<Integer, Long> toMonthMap(List<Object[]> rows) {
        Map<Integer, Long> map = new HashMap<>();
        for (Object[] row : rows) {
            int month = ((Number) row[0]).intValue();
            long count = ((Number) row[1]).longValue();
            map.put(month, count);
        }
        return map;
    }

    private AdminSystemStatsResponse.RecentUser mapToRecentUser(User user) {
        Boolean verified = null;
        if (user instanceof LandLord landlord) {
            verified = landlord.getVerified();
        } else if (user instanceof Customer customer) {
            verified = customer.getVerified();
        }

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String createdAtStr = user.getCreatedAt() != null
                ? user.getCreatedAt().format(fmt)
                : "";

        return AdminSystemStatsResponse.RecentUser.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .role(user.getRole() != null ? user.getRole().name() : "")
                .active(user.getActive() != null ? user.getActive() : true)
                .verified(verified)
                .createdAt(createdAtStr)
                .build();
    }
}
