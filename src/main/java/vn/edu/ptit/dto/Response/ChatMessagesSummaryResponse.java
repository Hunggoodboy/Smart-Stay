package vn.edu.ptit.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatMessagesSummaryResponse {
    private Long partnerId;
    private String partnerName;
    private String message;
    private LocalDateTime timestamp;
}
