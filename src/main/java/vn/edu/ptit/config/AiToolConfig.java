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
import vn.edu.ptit.dto.Response.RentalRequestResponse;
import vn.edu.ptit.entity.Customer;
import vn.edu.ptit.entity.RentalRequests;
import vn.edu.ptit.entity.User;
import vn.edu.ptit.repository.RentalRequestRepository;
import vn.edu.ptit.service.Authentication.AuthService;
import vn.edu.ptit.service.room.AppointmentService;
import vn.edu.ptit.service.roomManagement.RentalRequestService;
import jakarta.persistence.EntityManager;

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
    private final EntityManager entityManager;

    @Bean
    @Tool(description = "Tạo yêu cầu thuê phòng. Tham số bắt buộc: roomPostId (Long). Đối với address (String) và" +
            " idCardNumber (String): NẾU người dùng chưa cung cấp trong chat, HÃY TRUYỀN NULL." +
            "Tuyệt đối KHÔNG tự ý hỏi người dùng CCCD/Địa chỉ trước khi gọi hàm. Hệ thống backend sẽ tự động tìm trong Database.")
    public Function<AiRentalRequest, String> createRequestRentalForAi(){
        return request -> {
            RentalRequestDTO rentalRequestDTO = new RentalRequestDTO();
            User user = authService.getUser();
            String currentAddress = null;
            String currentIdCard = null;
            if (user instanceof Customer customer) {
                currentAddress = customer.getAddress();
                currentIdCard = customer.getIdCardNumber();
            } else if (user instanceof vn.edu.ptit.entity.LandLord landLord) {
                // Nếu là chủ nhà đi thuê phòng thì lấy thông tin của chủ nhà
                currentAddress = landLord.getAddress();
                currentIdCard = landLord.getIdCardNumber();
            }

            // 2. Ưu tiên dùng thông tin trong DB, nếu DB trống thì mới lấy từ AI (người dùng chat)
            String finalAddress = (currentAddress != null && !currentAddress.isBlank()) ? currentAddress : request.address();
            String finalIdCard = (currentIdCard != null && !currentIdCard.isBlank()) ? currentIdCard : request.idCardNumber();

            // 3. Nếu cả DB lẫn AI đều không có thông tin -> Trả lỗi bắt AI đi đòi người dùng
            if (finalAddress == null || finalAddress.isBlank() || finalIdCard == null || finalIdCard.isBlank()) {
                return "Hệ thống: Người dùng này chưa cập nhật đầy đủ Địa chỉ và CCCD. " +
                        "Bạn (AI) hãy phản hồi lại người dùng: 'Để tạo yêu cầu thuê phòng, bạn cần cung cấp thêm Địa chỉ và Số CCCD để hoàn thiện tài khoản. Vui lòng cung cấp thông tin này cho tôi nhé.'";
            }
            if(! (user instanceof Customer)){
                authService.upgradeCustomer(UpgradeCustomerRequest.builder()
                        .idCardNumber(finalIdCard)
                        .address(finalAddress)
                        .build());
            }
            // --- KIỂM TRA TRƯỚC CHO AI ĐỂ AI TRẢ LỜI MƯỢT MÀ HƠN ---
            // 1. Kiểm tra phòng đã có người thuê hoặc đã cọc chưa
            if (rentalRequestService.isRoomAlreadyRented(request.roomPostId())) {
                return "Hệ thống báo: Phòng này hiện đã có người thuê hoặc đã có người đặt cọc thành công. Hãy xin lỗi khách hàng và gợi ý họ chọn một phòng khác nhé.";
            }

            // 2. Kiểm tra xem bản thân khách hàng đã có phòng chưa
            if (rentalRequestService.isCustomerAlreadyRenting(user.getId())) {
                return "Hệ thống báo: Khách hàng này hiện đang thuê một phòng khác hoặc đang có hợp đồng hợp lệ. Quy định chỉ cho phép thuê 1 phòng. Hãy từ chối khách một cách lịch sự.";
            }

            try {
                rentalRequestDTO.setRoomPostId(request.roomPostId());
                ApiResponse response = rentalRequestService.createNewRentalRequest(rentalRequestDTO);
                return response.getMessage();
            }
            catch (Exception e){
                return "THẤT BẠI: " + e.getMessage() + ". Lệnh cho AI: Bắt buộc phải thông báo lỗi này nguyên văn cho khách hàng biết, không được nói là thành công.";
            }
        };
    }

    @Bean
    @Tool(description = "Lên lịch hẹn xem phòng. Tham số: rentalRequestId (Long)," +
            " appointmentTime (BẮT BUỘC ĐÚNG CHUẨN ISO-8601 yyyy-MM-ddTHH:mm:ss, VD: 2026-06-13T15:00:00), note (String, có thể null).")
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
                RentalRequests rentalRequest = rentalRequestRepository
                        .findById(request.rentalRequestId())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy yêu cầu thuê ID: " + request.rentalRequestId()));

                String resolvedLocation = rentalRequest.getRoomPost() != null
                        ? rentalRequest.getRoomPost().getAddress()
                        : "Địa chỉ chưa cập nhật";
                appointmentRequest.setLocation(resolvedLocation);
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
    @Tool(description = "Lấy danh sách yêu cầu thuê phòng được gửi hôm nay. Không cần tham số.")
    public Function<AiEmptyRequest, String> getTodayRentalRequestsForAi() {
        return request -> {
            try {
                Long userId = authService.getCurrentUserId();
                // Force clear JPA cache để lấy dữ liệu thời gian thực từ DB
                entityManager.clear();
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
    @Tool(description = "Lấy danh sách yêu cầu thuê phòng của người dùng hiện tại. Không cần tham số.")
    public Function<AiEmptyRequest, String> getRentalRequestsForAi() {
        return request -> {
            try {
                // Force clear JPA cache để luôn lấy trạng thái mới nhất từ DB
                entityManager.clear();
                List<RentalRequestResponse> requests = rentalRequestService.findMyRequests();

                if (requests == null || requests.isEmpty()) {
                    return "Bạn chưa có yêu cầu thuê phòng nào.";
                }

                StringBuilder sb = new StringBuilder();
                sb.append("Danh sách yêu cầu thuê phòng (").append(requests.size()).append(" yêu cầu):\n\n");

                for (int i = 0; i < requests.size(); i++) {
                    System.out.println();
                    RentalRequestResponse rr = requests.get(i);
                    System.out.println(rr);
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
                return "Không thể lấy danh sách yêu cầu thuê: " + e.getMessage();
            }
        };
    }

    @Bean
    @Tool(description = "Duyệt hoặc từ chối yêu cầu thuê. Tham số: rentalRequestId (Long), action (APPROVED | REJECTED).")
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
                return "THẤT BẠI: Không thể xử lý yêu cầu: " + e.getMessage() + ". Lệnh cho AI: Phải báo cho người dùng biết thao tác đã thất bại kèm theo lý do này.";
            }
        };
    }
}
