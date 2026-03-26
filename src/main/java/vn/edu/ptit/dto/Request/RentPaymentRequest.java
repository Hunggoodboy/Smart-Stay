package vn.edu.ptit.dto.Request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.ptit.entity.Contracts;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeeRequest {
    private double electricityNewIndex;
    private double waterNewIndex;
    private double waterOldIndex;
    private double electricityOldIndex;
    private LocalDateTime month;
    Contracts contract;
}
