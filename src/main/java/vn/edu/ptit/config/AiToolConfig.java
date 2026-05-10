package vn.edu.ptit.config;


import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;
import vn.edu.ptit.dto.Request.AiRentalRequest;
import vn.edu.ptit.dto.Request.RentalRequestDTO;
import vn.edu.ptit.dto.Response.ApiResponse;
import vn.edu.ptit.service.RentalRequestService;

import java.util.function.Function;

@Configuration
@AllArgsConstructor
public class AiToolConfig {
    private final RentalRequestService  rentalRequestService;

    @Bean
    @Description("Sử dụng hàm này khi người dùng yêu cầu thuê phòng này")
    public Function<AiRentalRequest, String> createRequestRentalForAi(){
        return request -> {
            RentalRequestDTO rentalRequestDTO = new RentalRequestDTO();
            try {
                rentalRequestDTO.setRoomPostId(request.roomPostId());
                ApiResponse response = rentalRequestService.createNewRentalRequest(rentalRequestDTO);
                return response.getMessage();
            }
            catch (Exception e){
                return "Hệ thống gặp lỗi khi tạo yêu cầu: " + e.getMessage() + ". Hãy báo khách hàng thử lại sau.";
            }
        };
    }
}
