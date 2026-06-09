package vn.edu.ptit.service.admin;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.ptit.dto.Request.AdminRoomPostFeaturedRequest;
import vn.edu.ptit.dto.Request.AdminRoomPostStatusRequest;
import vn.edu.ptit.dto.Request.AdminRoomStatusRequest;
import vn.edu.ptit.dto.Request.UpdateRoomPostRequest;
import vn.edu.ptit.dto.Response.AdminRoomPostResponse;
import vn.edu.ptit.dto.Response.AdminRoomResponse;
import vn.edu.ptit.dto.Response.ApiResponse;
import vn.edu.ptit.entity.RoomPosts;
import vn.edu.ptit.entity.Rooms;
import vn.edu.ptit.repository.AdminRoomPostRepository;
import vn.edu.ptit.repository.AdminRoomRepository;
import vn.edu.ptit.service.AI.VectorService;
import vn.edu.ptit.Exception.ResourceNotFoundException;
import vn.edu.ptit.Exception.InvalidRequestException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminRoomPostManagementService {
    private static final Logger log = LoggerFactory.getLogger(AdminRoomPostManagementService.class);

    private final AdminRoomPostRepository roomPostRepository;
    private final AdminRoomRepository roomRepository;
    private final VectorService vectorService;

    public List<AdminRoomPostResponse> getRoomPosts(RoomPosts.Status status, String keyword) {
        String normalizedKeyword = normalizeKeyword(keyword);
        List<RoomPosts> roomPosts = roomPostRepository.searchForAdmin(status, normalizedKeyword);
        List<AdminRoomPostResponse> responses = new ArrayList<>();

        for (RoomPosts post : roomPosts) {
            AdminRoomPostResponse response = toRoomPostResponse(post);
            responses.add(response);
        }

        return responses;
    }

    public List<AdminRoomResponse> getRooms(Rooms.Status status, String keyword) {
        String normalizedKeyword = normalizeKeyword(keyword);
        List<Rooms> rooms = roomRepository.searchForAdmin(status, normalizedKeyword);
        List<AdminRoomResponse> responses = new ArrayList<>();

        for (Rooms room : rooms) {
            AdminRoomResponse response = toRoomResponse(room);
            responses.add(response);
        }

        return responses;
    }

    @Transactional
    public AdminRoomPostResponse updateRoomPost(Long id, UpdateRoomPostRequest request) {
        RoomPosts post = roomPostRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bai dang", id));

        updateRoomPostInfo(post, request);
        post.setUpdatedAt(LocalDateTime.now());

        return toRoomPostResponse(roomPostRepository.save(post));
    }

    @Transactional
    public ApiResponse updateRoomPostStatus(Long id, AdminRoomPostStatusRequest request) {
        if (request.getStatus() == null) {
            throw new InvalidRequestException("Trang thai bai dang khong duoc de trong");
        }

        RoomPosts post = roomPostRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bai dang", id));

        applyRoomPostStatus(post, request.getStatus());
        post.setUpdatedAt(LocalDateTime.now());
        roomPostRepository.save(post);

        return ApiResponse.builder().success(true).message("Cap nhat trang thai bai dang thanh cong").build();
    }

    @Transactional
    public AdminRoomPostResponse updateRoomPostFeatured(Long id, AdminRoomPostFeaturedRequest request) {
        if (request.getFeatured() == null) {
            throw new InvalidRequestException("Trang thai noi bat khong duoc de trong");
        }

        RoomPosts post = roomPostRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bai dang", id));

        applyFeatured(post, request);
        post.setUpdatedAt(LocalDateTime.now());

        return toRoomPostResponse(roomPostRepository.save(post));
    }

    @Transactional
    public ApiResponse updateRoomStatus(Long id, AdminRoomStatusRequest request) {
        if (request.getStatus() == null) {
            throw new InvalidRequestException("Trang thai phong khong duoc de trong");
        }

        Rooms room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Phong", id));

        room.setStatus(request.getStatus());
        room.setUpdatedAt(LocalDateTime.now());
        roomRepository.save(room);

        return ApiResponse.builder().success(true).message("Cap nhat trang thai phong thanh cong").build();
    }

    private void updateRoomPostInfo(RoomPosts post, UpdateRoomPostRequest request) {
        // Chi cap nhat nhung truong duoc gui len, truong nao null thi giu nguyen.
        if (request.getTitle() != null) {
            post.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            post.setDescription(request.getDescription());
        }
        if (request.getMonthlyRent() != null) {
            post.setMonthlyRent(request.getMonthlyRent());
        }
        if (request.getDepositAmount() != null) {
            post.setDepositAmount(request.getDepositAmount());
        }
        if (request.getAreaM2() != null) {
            post.setAreaM2(request.getAreaM2());
        }
        if (request.getMaxOccupants() != null) {
            post.setMaxOccupants(request.getMaxOccupants());
        }
        if (request.getRoomType() != null) {
            post.setRoomType(request.getRoomType());
        }
        if (request.getAddress() != null) {
            post.setAddress(request.getAddress());
        }
        if (request.getWard() != null) {
            post.setWard(request.getWard());
        }
        if (request.getDistrict() != null) {
            post.setDistrict(request.getDistrict());
        }
        if (request.getCity() != null) {
            post.setCity(request.getCity());
        }
        if (request.getElectricityPricePerKwh() != null) {
            post.setElectricityPricePerKwh(request.getElectricityPricePerKwh());
        }
        if (request.getWaterPricePerM3() != null) {
            post.setWaterPricePerM3(request.getWaterPricePerM3());
        }
        if (request.getInternetFee() != null) {
            post.setInternetFee(request.getInternetFee());
        }
        if (request.getParkingFee() != null) {
            post.setParkingFee(request.getParkingFee());
        }
        if (request.getCleaningFee() != null) {
            post.setCleaningFee(request.getCleaningFee());
        }
        if (request.getExpiredAt() != null) {
            post.setExpiredAt(request.getExpiredAt());
        }
        if (request.getStatus() != null) {
            applyRoomPostStatus(post, request.getStatus());
        }
    }

    private void applyRoomPostStatus(RoomPosts post, RoomPosts.Status status) {
        RoomPosts.Status previousStatus = post.getStatus();
        post.setStatus(status);

        if (status == RoomPosts.Status.DELETED) {
            post.setDeletedAt(LocalDateTime.now());
        } else if (post.getDeletedAt() != null) {
            post.setDeletedAt(null);
        }

        if (status == RoomPosts.Status.ACTIVE && previousStatus != RoomPosts.Status.ACTIVE) {
            try {
                vectorService.addRoomPosts(post);
            } catch (RuntimeException ex) {
                log.warn("Khong the them vector cho bai dang id={}. Kiem tra API key AI/embedding.", post.getId(), ex);
            }
        }
    }

    private void applyFeatured(RoomPosts post, AdminRoomPostFeaturedRequest request) {
        if (Boolean.TRUE.equals(request.getFeatured())) {
            post.setFeatured(true);
            post.setFeaturedPriority(request.getFeaturedPriority() != null ? request.getFeaturedPriority() : 0);
            post.setFeaturedUntil(request.getFeaturedUntil());

            if (post.getFeaturedAt() == null) {
                post.setFeaturedAt(LocalDateTime.now());
            }
            return;
        }

        post.setFeatured(false);
        post.setFeaturedPriority(0);
        post.setFeaturedAt(null);
        post.setFeaturedUntil(null);
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return "";
        }
        return keyword.trim();
    }

    private AdminRoomPostResponse toRoomPostResponse(RoomPosts post) {
        return AdminRoomPostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .description(post.getDescription())
                .monthlyRent(post.getMonthlyRent())
                .depositAmount(post.getDepositAmount())
                .areaM2(post.getAreaM2())
                .maxOccupants(post.getMaxOccupants())
                .roomType(post.getRoomType())
                .address(post.getAddress())
                .ward(post.getWard())
                .district(post.getDistrict())
                .city(post.getCity())
                .status(post.getStatus())
                .mainImageUrl(post.getMainImageUrl())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .deletedAt(post.getDeletedAt())
                .featured(post.getFeatured())
                .featuredPriority(post.getFeaturedPriority())
                .featuredAt(post.getFeaturedAt())
                .featuredUntil(post.getFeaturedUntil())
                .landlordId(post.getLandlord() != null ? post.getLandlord().getId() : null)
                .landlordName(post.getLandlord() != null ? post.getLandlord().getFullName() : null)
                .landlordEmail(post.getLandlord() != null ? post.getLandlord().getEmail() : null)
                .build();
    }

    private AdminRoomResponse toRoomResponse(Rooms room) {
        return AdminRoomResponse.builder()
                .id(room.getId())
                .roomNumber(room.getRoomNumber())
                .roomType(room.getRoomType())
                .rentPrice(room.getRentPrice())
                .status(room.getStatus())
                .address(room.getAddress())
                .ward(room.getWard())
                .district(room.getDistrict())
                .city(room.getCity())
                .landlordId(room.getLandLord() != null ? room.getLandLord().getId() : null)
                .landlordName(room.getLandLord() != null ? room.getLandLord().getFullName() : null)
                .tenantName(room.getCustomer() != null ? room.getCustomer().getFullName() : null)
                .tenantEmail(room.getCustomer() != null ? room.getCustomer().getEmail() : null)
                .roomPostId(room.getRoomPost() != null ? room.getRoomPost().getId() : null)
                .createdAt(room.getCreatedAt())
                .updatedAt(room.getUpdatedAt())
                .deletedAt(room.getDeletedAt())
                .build();
    }
}
