package vn.edu.ptit.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 404 - Không tìm thấy resource (User, Room, Contract, RoomPost...)
    @ExceptionHandler(UserNotFoundException.class)
    ResponseEntity<?> handleUserNotFoundException(UserNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    ResponseEntity<?> handleResourceNotFoundException(ResourceNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // 400 - Dữ liệu đầu vào không hợp lệ
    @ExceptionHandler(InvalidRequestException.class)
    ResponseEntity<?> handleInvalidRequestException(InvalidRequestException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // 403 - Không có quyền truy cập
    @ExceptionHandler(UnauthorizedException.class)
    ResponseEntity<?> handleUnauthorizedException(UnauthorizedException ex) {
        return buildResponse(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    // 409 - Vi phạm quy tắc nghiệp vụ
    @ExceptionHandler(BusinessRuleException.class)
    ResponseEntity<?> handleBusinessRuleException(BusinessRuleException ex) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    // 502 - Lỗi thanh toán (PayOS, VietQR...)
    @ExceptionHandler(PaymentException.class)
    ResponseEntity<?> handlePaymentException(PaymentException ex) {
        return buildResponse(HttpStatus.BAD_GATEWAY, ex.getMessage());
    }

    // 401 - Token hết hạn
    @ExceptionHandler(TokenExpiredException.class)
    ResponseEntity<?> handleTokenExpiredException(TokenExpiredException ex) {
        return buildResponse(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    // Helper tạo body lỗi thống nhất
    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }
}
