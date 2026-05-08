package vn.edu.ptit.dto.Response;

import lombok.Builder;
import lombok.Data;
import vn.edu.ptit.entity.Rooms;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class LandlordHouseResponse {
    private Long id;
    private String roomNumber;
    private String roomType;
    private String address;
    private String ward;
    private String district;
    private String city;
    private String fullAddress;
    private Double areaM2;
    private Long maxOccupants;
    private Double rentPrice;
    private Rooms.Status status;
    private String description;
    private LocalDateTime createdAt;

    private TenantInfo tenant;
    private ContractInfo contract;

    @Data
    @Builder
    public static class TenantInfo {
        private Long id;
        private String fullName;
        private String phoneNumber;
        private String email;
        private String avatarUrl;
    }

    @Data
    @Builder
    public static class ContractInfo {
        private Long id;
        private String contractCode;
        private String status;
        private LocalDate startDate;
        private LocalDate endDate;
        private Double monthlyRent;
        private Double depositAmount;
        private Long billingDate;
    }
}
