package vn.edu.ptit.dto.Request;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Bộ lọc tìm kiếm bài đăng công khai — dùng cho trang khách hàng.
 * Tất cả tham số là optional; null = bỏ qua điều kiện đó.
 */
@Data
public class SearchRoomPostRequest {

    /** Từ khoá tìm trong tiêu đề và địa chỉ */
    private String keyword;

    private String city;
    private String district;

    private BigDecimal minPrice;
    private BigDecimal maxPrice;

    private Double minAreaM2;
    private Double maxAreaM2;

    private String roomType;

    /**
     * Sắp xếp kết quả:
     * "newest"     — bài đăng mới nhất (mặc định)
     * "price_asc"  — giá tăng dần
     * "price_desc" — giá giảm dần
     */
    private String sortBy = "newest";

    private Integer page = 0;

    private Integer size = 12;
}
