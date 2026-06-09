package vn.edu.ptit.Exception;

/**
 * Ném ra khi vi phạm quy tắc nghiệp vụ (business logic).
 * Ví dụ: phòng đã có người thuê, hợp đồng chưa được ký, hóa đơn đã tồn tại...
 * HTTP status: 409 CONFLICT
 */
public class BusinessRuleException extends RuntimeException {
    public BusinessRuleException(String message) {
        super(message);
    }
}
