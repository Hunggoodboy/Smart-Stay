package vn.edu.ptit.dto.Request;

/**
 * Input cho AI tool: Lên lịch hẹn xem phòng giữa chủ nhà và khách hàng.
 */
public record AiScheduleAppointmentRequest(
    Long rentalRequestId,
    String appointmentTime,
    String note
){}
