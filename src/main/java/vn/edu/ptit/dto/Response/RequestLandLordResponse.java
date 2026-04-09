package vn.edu.ptit.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.ptit.entity.LandLord;
import vn.edu.ptit.entity.User;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RequestLandLordResponse {
    private LandLord landLord;
    private User user;
}
