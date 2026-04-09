package vn.edu.ptit.service.Authentication;


import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import vn.edu.ptit.dto.Request.LandLordRegisterRequest;
import vn.edu.ptit.dto.Response.ApiResponse;
import vn.edu.ptit.dto.Response.RequestLandLordResponse;
import vn.edu.ptit.entity.LandLord;
import vn.edu.ptit.entity.User;
import vn.edu.ptit.repository.AdminRepository;
import vn.edu.ptit.repository.LandLordRepository;
import vn.edu.ptit.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class LandLordService {
    private final AuthService authService;
    private final LandLordRepository landLordRepository;
    private final AdminRepository adminRepository;
    private final UserRepository userRepository;
    public ApiResponse createLandLord(LandLordRegisterRequest request) {
        User currentUser = authService.getUser();
        LandLord landLord = new LandLord();
        landLord.setId(currentUser.getId());
        landLord.setAddress(request.getAddress());
        landLord.setIdCardNumber(request.getIdCardNumber());
        landLord.setVerified(false);
        landLordRepository.save(landLord);
        return ApiResponse.builder().message("Đăng ký chủ nhà thành công, vui lòng chờ xác thực").success(true).build();
    }
    private List<RequestLandLordResponse> convertToListResponse(List<LandLord> landLords) {
        return landLords.stream().map(landLord -> {
            RequestLandLordResponse response = RequestLandLordResponse.builder()
                    .landLord(landLord)
                    .user(userRepository.findById(landLord.getId()).orElse(null))
                    .build();
            return response;
        }).collect(Collectors.toList());
    }
    public List<RequestLandLordResponse> getAllLandLordsRequest() {
        List<LandLord> landLords = landLordRepository.findAll();
        return convertToListResponse(landLords);
    }
    public ApiResponse verifyLandLord(Long userId) {
        LandLord landLord = landLordRepository.findById(userId).orElse(null);
        landLord.setVerified(true);
        landLordRepository.save(landLord);
        return ApiResponse.builder().message("Xác thực chủ nhà thành công").success(true).build();
    }
    public List<RequestLandLordResponse> getLandLordsByVerified(boolean verified) {
        List<LandLord> landLords = landLordRepository.findLandLordByVerified(verified);
        return convertToListResponse(landLords);
    }
}
