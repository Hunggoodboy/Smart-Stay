package vn.edu.ptit.service.Authentication;


import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import vn.edu.ptit.dto.Request.LandLordRegisterRequest;
import vn.edu.ptit.dto.Response.ApiResponse;
import vn.edu.ptit.entity.LandLord;
import vn.edu.ptit.entity.User;
import vn.edu.ptit.repository.LandLordRepository;

@Service
@AllArgsConstructor
public class LandLordService {
    private final AuthService authService;
    private final LandLordRepository landLordRepository;

    private ApiResponse createLandLord(LandLordRegisterRequest request) {
        User currentUser = authService.getUser();
        LandLord landLord = new LandLord();
        landLord.setId(currentUser.getId());
        landLord.setAddress(request.getAddress());
        landLord.setIdCardNumber(request.getIdCardNumber());
        landLord.setVerified(false);
        landLordRepository.save(landLord);
        return ApiResponse.builder().message("Đăng ký chủ nhà thành công, vui lòng chờ xác thực").success(true).build();
    }
}
