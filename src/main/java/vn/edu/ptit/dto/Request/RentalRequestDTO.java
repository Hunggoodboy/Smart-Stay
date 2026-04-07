package vn.edu.ptit.dto.Request;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RentalRequestDTO {
    String message;
    private LocalDate desiredMoveInDate;
    private Integer desiredDurationMonths;
    private Integer numOccupants = 1;
    private Long roomPostId;

}
