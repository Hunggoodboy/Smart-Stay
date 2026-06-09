package vn.edu.ptit.Exception;

/**
 * Ném ra khi người dùng không có quyền thực hiện hành động.
 * HTTP status: 403 FORBIDDEN
 */
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
