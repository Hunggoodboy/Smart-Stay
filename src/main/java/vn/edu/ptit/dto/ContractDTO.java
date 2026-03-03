package vn.edu.ptit.dto;

import java.time.LocalDate;

public class ContractDTO {
    private Long id;
    private String contractCode;
    private LocalDate startDate;
    private LocalDate endDate;
    private Double monthlyRent;
    private String status;

    // Thông tin người thuê
    private String userName;
    private String userIdCardNumber;    // lấy từ Users

    // Thông tin chủ nhà
    private String customerName;
    private String customerIdCardNumber; // lấy từ Customers

    // Thông tin phòng
    private String roomNumber;
    private String roomAddress;
}
