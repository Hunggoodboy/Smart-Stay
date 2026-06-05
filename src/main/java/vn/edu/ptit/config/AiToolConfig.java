package vn.edu.ptit.config;


import lombok.AllArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import vn.edu.ptit.dto.Request.*;
import vn.edu.ptit.dto.Response.ApiResponse;
import vn.edu.ptit.dto.Response.AppointmentResponse;
import vn.edu.ptit.entity.Customer;
import vn.edu.ptit.entity.RentalRequests;
import vn.edu.ptit.entity.User;
import vn.edu.ptit.repository.RentalRequestRepository;
import vn.edu.ptit.service.Authentication.AuthService;
import vn.edu.ptit.service.room.AppointmentService;
import vn.edu.ptit.service.roomManagement.RentalRequestService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Function;

@Configuration
@Component
@AllArgsConstructor
public class AiToolConfig {
    private final RentalRequestService  rentalRequestService;
    private final AppointmentService appointmentService;
    private final AuthService authService;
    private final RentalRequestRepository rentalRequestRepository;

    @Bean
    @Tool(description = "Sử dụng hàm này khi người dùng yêu cầu thuê phòng. Bắt buộc truyền roomPostId. " +
            "Tham số 'address' là ĐỊA CHỈ NƠI Ở CỦA NGƯỜI DÙNG (Tuyệt đối KHÔNG sử dụng địa chỉ của phòng trọ/căn hộ). " +
            "Tham số 'idCardNumber' là số CCCD/CMND của người dùng. " +
            "NẾU người dùng CHƯA cung cấp rõ ràng thông tin cá nhân (địa chỉ và CCCD) trong câu chat, BẮT BUỘC để trống (null) cả 2 trường này.")
    public Function<AiRentalRequest, String> createRequestRentalForAi(){
        return request -> {
            RentalRequestDTO rentalRequestDTO = new RentalRequestDTO();
            User user = authService.getUser();
            if(! (user instanceof Customer)){
                if (request.address() == null || request.idCardNumber() == null) {
                    return "Hệ thống: Người dùng này chưa được nâng cấp thành Khách hàng. " +
                            "Bạn (AI) hãy phản hồi lại người dùng: 'Để thuê phòng, bạn cần cung cấp thêm Địa chỉ và Số CCCD để nâng cấp tài khoản. Vui lòng cung cấp cho tôi nhé.'";
                }
                authService.upgradeCustomer(UpgradeCustomerRequest.builder()
                        .idCardNumber(request.idCardNumber())
                        .address(request.address())
                        .build());
            }
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

    @Bean
    @Tool(description = "Sử dụng hàm này khi người dùng muốn lên lịch hẹn xem phòng với chủ nhà hoặc khách hàng. " +
                 "Cần truyền rentalRequestId (ID yêu cầu thuê), appointmentTime (thời gian hẹn dạng yyyy-MM-ddTHH:mm:ss), " +
                 "location (địa điểm hẹn), note (ghi chú, có thể để trống)")
    public Function<AiScheduleAppointmentRequest, String> scheduleAppointmentForAi() {
        return request -> {
            try {
                Long userId = authService.getCurrentUserId();

                // Parse thời gian hẹn từ String
                LocalDateTime appointmentTime;
                try {
                    appointmentTime = LocalDateTime.parse(request.appointmentTime(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                } catch (Exception e) {
                    return "Định dạng thời gian không hợp lệ. Vui lòng cung cấp thời gian theo định dạng yyyy-MM-ddTHH:mm:ss, ví dụ: 2026-06-05T10:00:00";
                }

                AppointmentRequest appointmentRequest = new AppointmentRequest();
                appointmentRequest.setRentalRequestId(request.rentalRequestId());
                appointmentRequest.setAppointmentTime(appointmentTime);
                appointmentRequest.setLocation(request.location());
                appointmentRequest.setNote(request.note() != null ? request.note() : "Lịch hẹn được tạo qua AI");

                AppointmentResponse response = appointmentService.createAppointment(appointmentRequest, userId);
                return String.format("Đã tạo lịch hẹn thành công!\n" +
                        "- ID lịch hẹn: %d\n" +
                        "- Thời gian: %s\n" +
                        "- Địa điểm: %s\n" +
                        "- Trạng thái: %s\n" +
                        "- Phòng: %s",
                        response.getId(),
                        response.getAppointmentTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                        response.getLocation(),
                        response.getStatus(),
                        response.getRoomPostTitle() != null ? response.getRoomPostTitle() : "N/A");
            } catch (Exception e) {
                return "Không thể tạo lịch hẹn: " + e.getMessage() + ". Hãy kiểm tra lại thông tin và thử lại.";
            }
        };
    }

    @Bean
    @Tool(description = "Sử dụng hàm này khi người dùng muốn xem tất cả các yêu cầu thuê phòng được gửi hôm nay. " +
                 "Không cần truyền tham số nào.")
    public Function<AiEmptyRequest, String> getTodayRentalRequestsForAi() {
        return request -> {
            try {
                Long userId = authService.getCurrentUserId();
                List<RentalRequests> todayRequests = rentalRequestRepository.findTodayRequestsByUserId(userId);

                if (todayRequests.isEmpty()) {
                    return "Hôm nay chưa có yêu cầu thuê phòng nào.";
                }

                StringBuilder sb = new StringBuilder();
                sb.append("Danh sách yêu cầu thuê phòng hôm nay (").append(todayRequests.size()).append(" yêu cầu):\n\n");

                for (int i = 0; i < todayRequests.size(); i++) {
                    RentalRequests rr = todayRequests.get(i);
                    sb.append(i + 1).append(". ");
                    sb.append("ID yêu cầu: ").append(rr.getId());
                    sb.append(" | Phòng: ").append(rr.getRoomPost() != null ? rr.getRoomPost().getTitle() : "N/A");
                    sb.append(" | Khách: ").append(rr.getCustomer() != null ? rr.getCustomer().getFullName() : "N/A");
                    sb.append(" | Chủ nhà: ").append(rr.getLandlord() != null ? rr.getLandlord().getFullName() : "N/A");
                    sb.append(" | Trạng thái: ").append(rr.getStatus());
                    sb.append(" | Thời gian gửi: ").append(rr.getCreatedAt() != null ?
                            rr.getCreatedAt().format(DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy")) : "N/A");
                    sb.append("\n");
                }

                return sb.toString();
            } catch (Exception e) {
                return "Không thể lấy danh sách yêu cầu thuê hôm nay: " + e.getMessage();
            }
        };
    }

    @Bean
    @Tool(description = "Sử dụng hàm này khi người dùng muốn duyệt (chấp nhận) hoặc từ chối yêu cầu thuê phòng. " +
                 "Cần truyền rentalRequestId (ID yêu cầu thuê) và action (APPROVED để duyệt, REJECTED để từ chối)")
    public Function<AiApproveRentalRequest, String> approveRentalRequestForAi() {
        return request -> {
            try {
                String action = request.action() != null ? request.action().toUpperCase().trim() : "";

                if (!action.equals("APPROVED") && !action.equals("REJECTED")) {
                    return "Hành động không hợp lệ. Chỉ chấp nhận: APPROVED (duyệt) hoặc REJECTED (từ chối).";
                }

                ApiResponse response = rentalRequestService.changeRequestStatus(request.rentalRequestId(), action);

                if (response.isSuccess()) {
                    String actionText = action.equals("APPROVED") ? "duyệt" : "từ chối";
                    return String.format("Đã %s yêu cầu thuê phòng (ID: %d) thành công! %s",
                            actionText, request.rentalRequestId(), response.getMessage());
                } else {
                    return "Thao tác thất bại: " + response.getMessage();
                }
            } catch (Exception e) {
                return "Không thể xử lý yêu cầu: " + e.getMessage() + ". Hãy kiểm tra lại ID yêu cầu thuê.";
            }
        };
    }
}
