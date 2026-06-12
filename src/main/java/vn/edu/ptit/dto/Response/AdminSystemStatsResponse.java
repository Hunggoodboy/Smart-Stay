package vn.edu.ptit.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminSystemStatsResponse {

    // 4 card thống kê tổng quan
    private long totalUsers;
    private long totalRooms;
    private long pendingApprovals;
    private long postsNeedReview;

    // Phân bố người dùng
    private long customerCount;
    private long landlordCount;
    private long adminCount;
    private long verifiedLandlords;
    private long totalLandlords;

    // Hoạt động theo tháng
    private List<MonthlyActivity> monthlyStats;

    // Người dùng gần đây
    private List<RecentUser> recentUsers;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyActivity {
        private int month;
        private long newRegistrations;
        private long roomsRented;
        private double revenue;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentUser {
        private Long id;
        private String fullName;
        private String role;
        private boolean active;
        private Boolean verified;
        private String createdAt;
    }
}
