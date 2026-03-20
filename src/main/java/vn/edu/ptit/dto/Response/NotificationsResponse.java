package vn.edu.ptit.dto.Response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationsResponse {
    private String content;
    private String notificationType;
    private String title;
    private LocalDateTime createdAt;
}
