package vn.edu.ptit.dto.Request;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminRoomPostFeaturedRequest {
    private Boolean featured;

    @PositiveOrZero(message = "Do uu tien noi bat phai lon hon hoac bang 0")
    private Integer featuredPriority;

    private LocalDateTime featuredUntil;
}
