package vn.edu.ptit.dto.request;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Bộ lọc tìm kiếm bài đăng công khai.
 */
@Data
public class SearchRoomPostRequest {

    private String keyword;
    private String city;
    private String district;
    private String ward;

    private BigDecimal minPrice;
    private BigDecimal maxPrice;

    private Double minAreaM2;
    private Double maxAreaM2;

    private String roomType;

    // Lọc theo tiện ích
    private Boolean hasWifi;
    private Boolean hasAirConditioner;
    private Boolean hasParking;
    private Boolean allowPet;
    private Boolean allowCooking;

    /**
     * Sắp xếp: "price_asc" | "price_desc" | "newest" | "most_viewed"
     * Mặc định: "newest"
     */
    private String sortBy = "newest";

    private Integer page = 0;
    private Integer size = 12;
}
