package vn.edu.ptit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LandlordInfo {
    private Long id;
    private String fullName;
    private String phoneNumber;
    private String avatarUrl;
}
