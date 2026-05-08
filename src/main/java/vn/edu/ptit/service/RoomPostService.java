package vn.edu.ptit.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.ptit.dto.LandlordInfo;
import vn.edu.ptit.dto.Request.CreateRoomPostRequest;
import vn.edu.ptit.dto.Response.ApiResponse;
import vn.edu.ptit.dto.Response.RoomPostDetailResponse;
import vn.edu.ptit.dto.Response.RoomPostSummaryResponse;
import vn.edu.ptit.entity.LandLord;
import vn.edu.ptit.entity.RoomPostImages;
import vn.edu.ptit.entity.RoomPosts;
import vn.edu.ptit.entity.User;
import vn.edu.ptit.repository.LandLordRepository;
import vn.edu.ptit.repository.RoomPostRepository;
import vn.edu.ptit.service.Authentication.AuthService;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@Service
@RequiredArgsConstructor
public class RoomPostService {
    private final RoomPostRepository roomPostRepository;
    private final AuthService authService;
    private final FileService fileService;
    private final LandLordRepository landLordRepository;

    @Transactional
    public ApiResponse createNewRoomPost(CreateRoomPostRequest createRoomPostRequest, MultipartFile mainImg,
            List<MultipartFile> imageUrls) throws IOException {
        RoomPosts roomPosts = new RoomPosts();
        BeanUtils.copyProperties(createRoomPostRequest, roomPosts);
        LandLord landLord = authService.getCurrentLandLord();
        if (landLord.getVerified() == false) {
            throw new InvalidParameterException("Bạn cần chờ admin xác thực tài khoản chủ nhà trước khi đăng bài");
        }
        roomPosts.setLandlord(landLord);
        roomPosts.setStatus(RoomPosts.Status.ACTIVE);
        roomPosts.setCreatedAt(LocalDateTime.now());
        if (mainImg != null && !mainImg.isEmpty()) {
            String url = fileService.saveImg(mainImg);
            roomPosts.setMainImageUrl(url);
        }
        if (imageUrls != null && !imageUrls.isEmpty()) {
            for (MultipartFile file : imageUrls) {
                RoomPostImages roomPostImages = new RoomPostImages();
                String url = fileService.saveImg(file);
                roomPostImages.setRoomPost(roomPosts);
                roomPostImages.setImageUrl(url);
            }
        }
        roomPostRepository.save(roomPosts);
        return ApiResponse.builder().message("Thêm phòng thành công").success(true).build();
    }

    public List<RoomPostSummaryResponse> getAllRoomPosts() {
        List<RoomPosts> roomPosts = roomPostRepository.findAll();
        return roomPosts.stream().map(post -> {
            return RoomPostSummaryResponse.builder()
                    .id(post.getId())
                    .title(post.getTitle())
                    .monthlyRent(post.getMonthlyRent())
                    .areaM2(post.getAreaM2())
                    .roomType(post.getRoomType())
                    .status(post.getStatus())
                    .shortAddress(buildShortAddress(post))
                    .thumbnailUrl(post.getMainImageUrl())
                    .landlordName(post.getLandlord().getFullName())
                    .publishedAt(post.getCreatedAt())
                    .build();
        }).toList();
    }

    public List<RoomPostSummaryResponse> getPostsForCurrentLandlord() {
        Long landlordId = authService.getCurrentUserId();
        Page<RoomPosts> page = roomPostRepository.findByLandlordIdOrderByCreatedAtDesc(landlordId,
                PageRequest.of(0, 100));
        return page.stream().map(post -> RoomPostSummaryResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .monthlyRent(post.getMonthlyRent())
                .areaM2(post.getAreaM2())
                .roomType(post.getRoomType())
                .status(post.getStatus())
                .shortAddress(buildShortAddress(post))
                .thumbnailUrl(post.getMainImageUrl())
                .landlordName(post.getLandlord().getFullName())
                .publishedAt(post.getCreatedAt())
                .build())
                .toList();
    }

    public RoomPostDetailResponse getRoomPostDetail(Long id) {
        RoomPosts room = roomPostRepository.findById(id).orElseThrow();
        User user = room.getLandlord();
        RoomPostDetailResponse response = new RoomPostDetailResponse();
        BeanUtils.copyProperties(room, response);
        List<RoomPostImages> images = room.getImages();
        System.out.println(images.size());
        response.setImages(images.stream()
                .map(image -> image.getImageUrl())
                .toList());
        response.setFullAddress(buildFullAddress(room));
        response.setLandlord(
                new LandlordInfo(user.getId(), user.getFullName(), user.getPhoneNumber(), user.getAvatarUrl()));
        return response;
    }

    private String buildFullAddress(RoomPosts post) {
        return Stream.of(post.getAddress(), post.getWard(), post.getDistrict(), post.getCity())
                .filter(value -> value != null && !value.isBlank())
                .collect(Collectors.joining(", "));
    }

    private String buildShortAddress(RoomPosts post) {
        String shortAddress = Stream.of(post.getDistrict(), post.getCity())
                .filter(value -> value != null && !value.isBlank())
                .collect(Collectors.joining(", "));
        if (!shortAddress.isBlank()) {
            return shortAddress;
        }
        return Stream.of(post.getAddress(), post.getWard(), post.getCity())
                .filter(value -> value != null && !value.isBlank())
                .collect(Collectors.joining(", "));
    }

}
