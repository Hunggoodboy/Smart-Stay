package vn.edu.ptit.Exception;

/**
 * Ném ra khi JWT access token hoặc refresh token đã hết hạn.
 * HTTP status: 401 UNAUTHORIZED
 */
public class TokenExpiredException extends RuntimeException {
    public TokenExpiredException(String message) {
        super(message);
    }
}
