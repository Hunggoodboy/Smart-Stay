package vn.edu.ptit.Exception;

/**
 * Ném ra khi xảy ra lỗi trong quá trình xử lý thanh toán (PayOS, VietQR...).
 * HTTP status: 502 BAD_GATEWAY
 */
public class PaymentException extends RuntimeException {
    public PaymentException(String message) {
        super(message);
    }

    public PaymentException(String message, Throwable cause) {
        super(message, cause);
    }
}
