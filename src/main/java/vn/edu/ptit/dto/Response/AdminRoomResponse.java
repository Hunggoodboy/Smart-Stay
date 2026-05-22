package vn.edu.ptit.dto.Response;

import lombok.Builder;
import lombok.Data;
import vn.edu.ptit.entity.Rooms;

import java.time.LocalDateTime;

@Data
@Builder
public class AdminRoomResponse {
    private Long id;
    private String roomNumber;
    private String roomType;
    private Double rentPrice;
    private Rooms.Status status;
    private String address;
    private String ward;
    private String district;
    private String city;
    private Long landlordId;
    private String landlordName;
    private String tenantName;
    private String tenantEmail;
    private Long roomPostId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
}
