package vn.edu.ptit.dto.Response;

import lombok.Builder;
import lombok.Data;
import vn.edu.ptit.entity.RoomPosts;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class AdminRoomPostResponse {
    private Long id;
    private String title;
    private String description;
    private BigDecimal monthlyRent;
    private BigDecimal depositAmount;
    private Double areaM2;
    private Integer maxOccupants;
    private String roomType;
    private String address;
    private String ward;
    private String district;
    private String city;
    private RoomPosts.Status status;
    private String mainImageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    private Boolean featured;
    private Integer featuredPriority;
    private LocalDateTime featuredAt;
    private LocalDateTime featuredUntil;
    private Long landlordId;
    private String landlordName;
    private String landlordEmail;
}
