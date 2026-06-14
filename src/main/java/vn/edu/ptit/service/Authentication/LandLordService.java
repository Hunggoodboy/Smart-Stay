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
        
        // 1. Kiểm tra xem người dùng có phải là Customer hoặc Admin không
        if (currentUser instanceof vn.edu.ptit.entity.Customer || currentUser instanceof vn.edu.ptit.entity.Admin) {
            return ApiResponse.builder()
                    .message("Tài khoản của bạn không được phép đăng ký làm chủ nhà (Chỉ dành cho USER bình thường)")
                    .success(false)
                    .build();
        }

        // 2. Kiểm tra xem người dùng đã từng gửi yêu cầu đăng ký chưa
        Boolean isVerified = userRepository.getLandlordVerificationStatus(currentUser.getId());
        if (isVerified != null) {
            if (Boolean.TRUE.equals(isVerified)) {
                return ApiResponse.builder()
                        .message("Bạn đã là chủ nhà rồi, không thể đăng ký lại")
                        .success(false)
                        .build();
            } else {
                return ApiResponse.builder()
                        .message("Bạn đã gửi yêu cầu rồi, vui lòng chờ Quản trị viên xác thực")
                        .success(false)
                        .build();
            }
        }

        // 3. Nếu hợp lệ, tạo mới yêu cầu
        userRepository.insertIntoLandLord(currentUser.getId(), request.getAddress(), request.getIdCardNumber());
        
        return ApiResponse.builder()
                .message("Đăng ký chủ nhà thành công, vui lòng chờ xác thực")
                .success(true)
                .build();
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
        User currentUser = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));
        userRepository.updateUserType("LANDLORD", currentUser.getId());
        LandLord landLord = landLordRepository.findById(userId).orElse(null);
        if (landLord != null) {
            landLord.setVerified(true);
            landLordRepository.save(landLord);
        }
        return ApiResponse.builder().message("Xác thực chủ nhà thành công").success(true).build();
    }
    public List<RequestLandLordResponse> getLandLordsByVerified(boolean verified) {
        List<LandLord> landLords = landLordRepository.findLandLordByVerified(verified);
        return convertToListResponse(landLords);
    }
}
