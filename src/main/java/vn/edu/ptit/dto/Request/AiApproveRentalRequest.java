package vn.edu.ptit.dto.Request;

/**
 * Input cho AI tool: Duyệt hoặc từ chối yêu cầu thuê phòng.
 */
public record AiApproveRentalRequest(
    Long rentalRequestId,
    String action
){}
