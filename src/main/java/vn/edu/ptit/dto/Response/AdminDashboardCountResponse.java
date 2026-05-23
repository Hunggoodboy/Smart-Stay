package vn.edu.ptit.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AdminDashboardCountResponse {
    private String key;
    private long value;
}
