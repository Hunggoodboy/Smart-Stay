package vn.edu.ptit.Exception;

/**
 * Ném ra khi không tìm thấy một resource (User, Room, Contract, RoomPost, ...) trong DB.
 * HTTP status: 404 NOT_FOUND
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceName, Long id) {
        super("Không tìm thấy " + resourceName + " với id: " + id);
    }

    public ResourceNotFoundException(String resourceName, String field, Object value) {
        super("Không tìm thấy " + resourceName + " với " + field + " = " + value);
    }
}
