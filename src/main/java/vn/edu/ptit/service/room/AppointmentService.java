package vn.edu.ptit.service.room;

import lombok.AllArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import vn.edu.ptit.dto.Request.AppointmentRequest;
import vn.edu.ptit.dto.Response.AppointmentResponse;
import vn.edu.ptit.dto.Response.AppointmentSummaryResponse;
import vn.edu.ptit.entity.Appointments;
import vn.edu.ptit.entity.RentalRequests;
import vn.edu.ptit.entity.LandLord;
import vn.edu.ptit.entity.Customer;
import vn.edu.ptit.entity.User;
import vn.edu.ptit.repository.AppointmentRepository;
import vn.edu.ptit.repository.RentalRequestRepository;
import vn.edu.ptit.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;

    private final RentalRequestRepository rentalRequestRepository;

    private final UserRepository userRepository;

    /**
     * Tạo yêu cầu hẹn lịch (cho cả chủ nhà và khách hàng)
     */
    @Transactional
    public AppointmentResponse createAppointment(AppointmentRequest request, Long userId) throws BadRequestException {
        // Tìm yêu cầu thuê
        RentalRequests rentalRequest = rentalRequestRepository.findById(request.getRentalRequestId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy yêu cầu thuê với ID: " + request.getRentalRequestId()));

        // Lấy thông tin user hiện tại
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + userId));

        // Kiểm tra quyền: Người tạo phải là khách hàng gửi yêu cầu hoặc chủ nhà nhận yêu cầu
        if (!user.getId().equals(rentalRequest.getCustomer().getId()) &&
            !user.getId().equals(rentalRequest.getLandlord().getId())) {
            throw new BadRequestException("Bạn không có quyền tạo lịch hẹn cho yêu cầu thuê này");
        }

        // Kiểm tra trạng thái yêu cầu thuê: Phải là APPROVED mới hẹn gặp
        if (!rentalRequest.getStatus().equals(RentalRequests.Status.APPROVED)) {
            throw new BadRequestException("Yêu cầu thuê phải được duyệt trước khi tạo lịch hẹn");
        }

        // Kiểm tra nếu lịch hẹn đã tồn tại cho yêu cầu thuê này
        if (appointmentRepository.existsByRentalRequestId(request.getRentalRequestId())) {
            throw new BadRequestException("Đã tồn tại lịch hẹn cho yêu cầu thuê này");
        }

        // Kiểm tra thời gian hẹn hợp lệ (phải sau hiện tại ít nhất 1 giờ)
        LocalDateTime now = LocalDateTime.now();
        if (request.getAppointmentTime().isBefore(now.plusHours(1))) {
            throw new BadRequestException("Thời gian hẹn phải cách hiện tại ít nhất 1 giờ");
        }

        // Tạo mới Appointment
        Appointments appointment = new Appointments();
        appointment.setRentalRequest(rentalRequest);
        appointment.setRoomPost(rentalRequest.getRoomPost());
        appointment.setCustomer(rentalRequest.getCustomer());
        appointment.setLandlord(rentalRequest.getLandlord());
        appointment.setAppointmentTime(request.getAppointmentTime());
        appointment.setLocation(request.getLocation());
        appointment.setNote(request.getNote());
        appointment.setCreatedBy(user);
        
        // Thiết lập trạng thái ban đầu:
        // - Nếu chủ nhà tạo: PENDING (Chờ người thuê xác nhận)
        // - Nếu người thuê tạo: CONFIRMED_BY_TENANT (Chờ chủ nhà xác nhận)
        if (user instanceof LandLord) {
            appointment.setStatus(Appointments.Status.PENDING);
        } else {
            appointment.setStatus(Appointments.Status.CONFIRMED_BY_TENANT);
        }
        
        appointment.setCreatedAt(now);
        appointment.setUpdatedAt(now);

        Appointments savedAppointment = appointmentRepository.save(appointment);
        return convertToResponse(savedAppointment);
    }

    /**
     * Lấy chi tiết lịch hẹn
     */
    public AppointmentResponse getAppointmentById(Long appointmentId, Long userId) throws BadRequestException {
        Appointments appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lịch hẹn với ID: " + appointmentId));

        // Kiểm tra quyền xem lịch hẹn
        if (!isAuthorized(appointment, userId)) {
            throw new BadRequestException("Bạn không có quyền xem lịch hẹn này");
        }

        return convertToResponse(appointment);
    }

    /**
     * Lấy danh sách lịch hẹn của người dùng hiện tại (tóm tắt)
     */
    public List<AppointmentSummaryResponse> getMyAppointments(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + userId));

        List<Appointments> appointments;
        if (user instanceof LandLord) {
            appointments = appointmentRepository.findByLandlordId(userId);
        } else {
            appointments = appointmentRepository.findByCustomerId(userId);
        }

        return appointments.stream()
                .map(this::convertToSummaryResponse)
                .collect(Collectors.toList());
    }

    /**
     * Cập nhật thông tin lịch hẹn (chỉ người tạo hoặc người trong cuộc mới có quyền, 
     * trạng thái sẽ được reset về trạng thái chờ duyệt nếu thời gian/địa điểm thay đổi)
     */
    @Transactional
    public AppointmentResponse updateAppointment(Long appointmentId, AppointmentRequest request, Long userId) throws BadRequestException {
        Appointments appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lịch hẹn với ID: " + appointmentId));

        // Kiểm tra quyền cập nhật
        if (!isAuthorized(appointment, userId)) {
            throw new BadRequestException("Bạn không có quyền cập nhật lịch hẹn này");
        }

        // Không cho phép sửa nếu lịch đã hoàn tất hoặc đã hủy
        if (appointment.getStatus().equals(Appointments.Status.COMPLETED) ||
            appointment.getStatus().equals(Appointments.Status.CANCELLED)) {
            throw new BadRequestException("Không thể cập nhật lịch hẹn đã hoàn tất hoặc đã hủy");
        }

        // Cập nhật thông tin
        boolean isChanged = false;
        if (request.getAppointmentTime() != null && !request.getAppointmentTime().equals(appointment.getAppointmentTime())) {
            if (request.getAppointmentTime().isBefore(LocalDateTime.now().plusHours(1))) {
                throw new BadRequestException("Thời gian hẹn phải cách hiện tại ít nhất 1 giờ");
            }
            appointment.setAppointmentTime(request.getAppointmentTime());
            isChanged = true;
        }

        if (request.getLocation() != null && !request.getLocation().equals(appointment.getLocation())) {
            appointment.setLocation(request.getLocation());
            isChanged = true;
        }

        if (request.getNote() != null) {
            appointment.setNote(request.getNote());
        }

        // Nếu có sự thay đổi về thời gian hoặc địa điểm, cần reset lại trạng thái chờ xác nhận
        if (isChanged) {
            User user = userRepository.findById(userId).orElseThrow();
            if (user instanceof LandLord) {
                appointment.setStatus(Appointments.Status.PENDING);
            } else {
                appointment.setStatus(Appointments.Status.CONFIRMED_BY_TENANT);
            }
            appointment.setCreatedBy(user);
        }

        appointment.setUpdatedAt(LocalDateTime.now());
        Appointments updatedAppointment = appointmentRepository.save(appointment);
        return convertToResponse(updatedAppointment);
    }

    /**
     * Đồng ý/Xác nhận lịch hẹn
     */
    @Transactional
    public AppointmentResponse confirmAppointment(Long appointmentId, Long userId) throws BadRequestException {
        Appointments appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lịch hẹn với ID: " + appointmentId));

        // Kiểm tra quyền tham gia lịch hẹn
        if (!isAuthorized(appointment, userId)) {
            throw new BadRequestException("Bạn không có quyền xác nhận lịch hẹn này");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + userId));

        Appointments.Status currentStatus = appointment.getStatus();

        if (currentStatus.equals(Appointments.Status.CONFIRMED_BY_BOTH)) {
            throw new BadRequestException("Lịch hẹn đã được xác nhận bởi cả hai bên");
        }
        if (currentStatus.equals(Appointments.Status.CANCELLED) || currentStatus.equals(Appointments.Status.COMPLETED)) {
            throw new BadRequestException("Không thể xác nhận lịch hẹn đã hủy hoặc đã hoàn tất");
        }

        // Xử lý xác nhận theo vai trò
        if (user instanceof LandLord) {
            if (currentStatus.equals(Appointments.Status.CONFIRMED_BY_TENANT)) {
                appointment.setStatus(Appointments.Status.CONFIRMED_BY_BOTH);
            } else if (currentStatus.equals(Appointments.Status.PENDING)) {
                appointment.setStatus(Appointments.Status.CONFIRMED_BY_LANDLORD);
            } else {
                throw new BadRequestException("Hành động không hợp lệ đối với Chủ nhà dựa trên trạng thái hiện tại của lịch hẹn");
            }
        } else if (user instanceof Customer) {
            if (currentStatus.equals(Appointments.Status.PENDING) || currentStatus.equals(Appointments.Status.CONFIRMED_BY_LANDLORD)) {
                appointment.setStatus(Appointments.Status.CONFIRMED_BY_BOTH);
            } else if (currentStatus.equals(Appointments.Status.CONFIRMED_BY_TENANT)) {
                throw new BadRequestException("Bạn đã xác nhận lịch hẹn này rồi");
            } else {
                throw new BadRequestException("Hành động không hợp lệ đối với Người thuê dựa trên trạng thái hiện tại của lịch hẹn");
            }
        }

        appointment.setUpdatedAt(LocalDateTime.now());
        Appointments saved = appointmentRepository.save(appointment);
        return convertToResponse(saved);
    }

    /**
     * Hủy lịch hẹn
     */
    @Transactional
    public AppointmentResponse cancelAppointment(Long appointmentId, Long userId) throws BadRequestException {
        Appointments appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lịch hẹn với ID: " + appointmentId));

        // Kiểm tra quyền hủy
        if (!isAuthorized(appointment, userId)) {
            throw new BadRequestException("Bạn không có quyền hủy lịch hẹn này");
        }

        if (appointment.getStatus().equals(Appointments.Status.CANCELLED)) {
            throw new BadRequestException("Lịch hẹn đã bị hủy trước đó");
        }

        // Chỉ cho phép hủy trước giờ hẹn ít nhất 2 giờ
        if (appointment.getAppointmentTime().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new BadRequestException("Không thể hủy lịch hẹn ít hơn 2 giờ trước thời gian đã hẹn");
        }

        appointment.setStatus(Appointments.Status.CANCELLED);
        appointment.setUpdatedAt(LocalDateTime.now());

        Appointments saved = appointmentRepository.save(appointment);
        return convertToResponse(saved);
    }

    /**
     * Hoàn tất lịch hẹn (Thường do chủ nhà bấm xác nhận sau khi gặp mặt xong)
     */
    @Transactional
    public AppointmentResponse completeAppointment(Long appointmentId, Long userId) throws BadRequestException {
        Appointments appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lịch hẹn với ID: " + appointmentId));

        // Chỉ chủ nhà mới được bấm hoàn tất
        if (!appointment.getLandlord().getId().equals(userId)) {
            throw new BadRequestException("Chỉ chủ nhà mới có thể hoàn tất lịch hẹn");
        }

        if (!appointment.getStatus().equals(Appointments.Status.CONFIRMED_BY_BOTH)) {
            throw new BadRequestException("Lịch hẹn phải được xác nhận bởi cả hai bên trước khi hoàn tất");
        }

        appointment.setStatus(Appointments.Status.COMPLETED);
        appointment.setUpdatedAt(LocalDateTime.now());

        Appointments saved = appointmentRepository.save(appointment);
        return convertToResponse(saved);
    }

    // ==================== HELPER METHODS ====================

    private boolean isAuthorized(Appointments appointment, Long userId) {
        return appointment.getCustomer().getId().equals(userId) ||
               appointment.getLandlord().getId().equals(userId);
    }

    private AppointmentResponse convertToResponse(Appointments appointment) {
        AppointmentResponse response = new AppointmentResponse();
        response.setId(appointment.getId());
        response.setAppointmentTime(appointment.getAppointmentTime());
        response.setLocation(appointment.getLocation());
        response.setNote(appointment.getNote());
        response.setStatus(appointment.getStatus());
        response.setCreatedAt(appointment.getCreatedAt());
        response.setUpdatedAt(appointment.getUpdatedAt());

        if (appointment.getRentalRequest() != null) {
            response.setRentalRequestId(appointment.getRentalRequest().getId());
        }

        if (appointment.getRoomPost() != null) {
            response.setRoomPostId(appointment.getRoomPost().getId());
            response.setRoomPostTitle(appointment.getRoomPost().getTitle());
            response.setRoomPostAddress(appointment.getRoomPost().getAddress());
            response.setRoomPostMainImageUrl(appointment.getRoomPost().getMainImageUrl());
        }

        if (appointment.getLandlord() != null) {
            response.setLandlordId(appointment.getLandlord().getId());
            response.setLandlordName(appointment.getLandlord().getFullName());
            response.setLandlordPhone(appointment.getLandlord().getPhoneNumber());
            response.setLandlordEmail(appointment.getLandlord().getEmail());
            response.setLandlordAvatarUrl(appointment.getLandlord().getAvatarUrl());
        }

        if (appointment.getCustomer() != null) {
            response.setCustomerId(appointment.getCustomer().getId());
            response.setCustomerName(appointment.getCustomer().getFullName());
            response.setCustomerPhone(appointment.getCustomer().getPhoneNumber());
            response.setCustomerEmail(appointment.getCustomer().getEmail());
            response.setCustomerAvatarUrl(appointment.getCustomer().getAvatarUrl());
        }

        return response;
    }

    private AppointmentSummaryResponse convertToSummaryResponse(Appointments appointment) {
        AppointmentSummaryResponse response = new AppointmentSummaryResponse();
        response.setId(appointment.getId());
        response.setAppointmentTime(appointment.getAppointmentTime());
        response.setLocation(appointment.getLocation());
        response.setStatus(appointment.getStatus());
        response.setCreatedAt(appointment.getCreatedAt());

        if (appointment.getRoomPost() != null) {
            response.setRoomPostId(appointment.getRoomPost().getId());
            response.setRoomPostTitle(appointment.getRoomPost().getTitle());
            response.setRoomPostMainImageUrl(appointment.getRoomPost().getMainImageUrl());
        }

        if (appointment.getLandlord() != null) {
            response.setLandlordId(appointment.getLandlord().getId());
            response.setLandlordName(appointment.getLandlord().getFullName());
            response.setLandlordAvatarUrl(appointment.getLandlord().getAvatarUrl());
        }

        if (appointment.getCustomer() != null) {
            response.setCustomerId(appointment.getCustomer().getId());
            response.setCustomerName(appointment.getCustomer().getFullName());
            response.setCustomerAvatarUrl(appointment.getCustomer().getAvatarUrl());
        }

        return response;
    }
}
