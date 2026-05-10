package vn.edu.ptit.dto.Request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateNotificationRequest {
    private Long roomId;
    private String title;
    private String content;
    private String notificationType;
    private Long CustomerId;
}
