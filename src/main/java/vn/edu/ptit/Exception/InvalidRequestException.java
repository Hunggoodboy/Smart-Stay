package vn.edu.ptit.Exception;

/**
 * Ném ra khi dữ liệu đầu vào không hợp lệ hoặc thiếu trường bắt buộc.
 * HTTP status: 400 BAD_REQUEST
 */
public class InvalidRequestException extends RuntimeException {
    public InvalidRequestException(String message) {
        super(message);
    }
}
