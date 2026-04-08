package vn.edu.ptit.dto.Request;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LandLordRegisterRequest {
    private String idCardNumber;
    private String address;

}
