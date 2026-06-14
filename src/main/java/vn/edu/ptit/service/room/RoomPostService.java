package vn.edu.ptit.service.room;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.ptit.dto.LandlordInfo;
import vn.edu.ptit.dto.Request.CreateRoomPostRequest;
import vn.edu.ptit.dto.Response.ApiResponseCreateRoomPost;
import vn.edu.ptit.dto.Response.RoomPostDetailResponse;
import vn.edu.ptit.dto.Response.RoomPostSummaryResponse;
import vn.edu.ptit.entity.LandLord;
import vn.edu.ptit.entity.RoomPostImages;
import vn.edu.ptit.entity.RoomPosts;
import vn.edu.ptit.entity.Rooms;
import vn.edu.ptit.entity.User;
import vn.edu.ptit.repository.RoomPostRepository;
import vn.edu.ptit.repository.RoomsRepository;
import vn.edu.ptit.service.Authentication.AuthService;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@Service
@RequiredArgsConstructor
public class RoomPostService {
    private final RoomPostRepository roomPostRepository;
    private final AuthService authService;
    private final FileService fileService;
    private final RoomsRepository roomsRepository;

    @Transactional
    public ApiResponseCreateRoomPost createNewRoomPost(CreateRoomPostRequest createRoomPostRequest, MultipartFile mainImg,
                                                       List<MultipartFile> imageUrls) throws IOException {
        RoomPosts roomPosts = new RoomPosts();
        BeanUtils.copyProperties(createRoomPostRequest, roomPosts);
        LandLord landLord = authService.getCurrentLandLord();
        if (landLord.getVerified() == false) {
            throw new InvalidParameterException("Bạn cần chờ admin xác thực tài khoản chủ nhà trước khi đăng bài");
        }
        roomPosts.setLandlord(landLord);
        roomPosts.setStatus(RoomPosts.Status.DRAFT);
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
        RoomPosts save = roomPostRepository.save(roomPosts);
        return ApiResponseCreateRoomPost.builder()
                .message("Thêm phòng thành công")
                .success(true)
                .roomPostId(save.getId()).build();
    }

    public Page<RoomPostSummaryResponse> getRoomPostsFeed(int page, int size) {
        Long userId = authService.getCurrentUserIdOrNull();

        // Thêm hàm lấy Role của người dùng hiện tại
        String role = authService.getUser().getRole().toString();

        Sort sort = Sort.by(Sort.Direction.DESC, "featuredPriority")
                .and(Sort.by(Sort.Direction.DESC, "createdAt"));
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<RoomPosts> roomPostsPage;

        if (userId != null && "LANDLORD".equals(role)) {
            // Chỉ Landlord mới bị giấu phòng của chính mình
            roomPostsPage = roomPostRepository.findAllActiveRoomsWithoutMine(userId, pageable);
        } else {
            // Customer, Admin hoặc người chưa đăng nhập (Guest) thì xem được toàn bộ
            roomPostsPage = roomPostRepository.findAllActiveRooms(pageable);
        }

        return roomPostsPage.map(post ->
                RoomPostSummaryResponse.builder()
                        .id(post.getId())
                        .title(post.getTitle())
                        .monthlyRent(post.getMonthlyRent())
                        .areaM2(post.getAreaM2())
                        .roomType(post.getRoomType())
                        .status(post.getStatus())
                        .thumbnailUrl(post.getMainImageUrl())
                        .landlordName(post.getLandlord().getFullName())
                        .publishedAt(post.getCreatedAt())
                        .build()
        );
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
        response.setFullAddress(room.getAddress());
        boolean isOwner = false;

        try {
            // Thử lấy ID người dùng hiện tại
            Long currentUserId = authService.getCurrentUserIdOrNull();
            System.out.println("Current user id: " + currentUserId);
            System.out.println("Room landlord id: " + room.getLandlord().getId());
            // Nếu lấy được và trùng với ID chủ bài đăng
            if (currentUserId != null && room.getLandlord().getId().equals(currentUserId)) {
                isOwner = true;
            }
        } catch (Exception e) {
            // Bắt lỗi "Người dùng chưa đăng nhập" hoặc token hết hạn
            // Không làm gì cả, cứ để isOwner = false để họ xem với tư cách Khách
        }
        response.setOwner(isOwner);
        response.setLandlord(new LandlordInfo(user.getId(), user.getFullName(), user.getPhoneNumber(), user.getAvatarUrl()));

        // Kiểm tra phòng quản lý đã tồn tại trong bảng rooms chưa
        java.util.Optional<Rooms> existingRoom = roomsRepository.findByRoomPostId(id);
        if (existingRoom.isPresent()) {
            response.setHasRoom(true);
            response.setRoomStatus(existingRoom.get().getStatus().name());
        } else {
            response.setHasRoom(false);
            response.setRoomStatus(null);
        }

        return response;
    }

    @Transactional
    public void updateRoomPost(Long id, CreateRoomPostRequest request,
                               MultipartFile mainImg, List<MultipartFile> gallery) throws IOException {
        RoomPosts roomPost = roomPostRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bài đăng không tồn tại"));

        // Kiểm tra quyền sở hữu
        Long currentUserId = authService.getCurrentUserId();
        if (!roomPost.getLandlord().getId().equals(currentUserId)) {
            throw new RuntimeException("Bạn không có quyền chỉnh sửa bài đăng này");
        }

        // Cập nhật các trường thông tin
        if (request.getTitle() != null) roomPost.setTitle(request.getTitle());
        if (request.getDescription() != null) roomPost.setDescription(request.getDescription());
        if (request.getMonthlyRent() != null) roomPost.setMonthlyRent(request.getMonthlyRent());
        if (request.getDepositAmount() != null) roomPost.setDepositAmount(request.getDepositAmount());
        if (request.getAreaM2() != null) roomPost.setAreaM2(request.getAreaM2());
        if (request.getMaxOccupants() != null) roomPost.setMaxOccupants(request.getMaxOccupants());
        if (request.getRoomType() != null) roomPost.setRoomType(request.getRoomType());
        if (request.getAddress() != null) roomPost.setAddress(request.getAddress());
        if (request.getWard() != null) roomPost.setWard(request.getWard());
        if (request.getDistrict() != null) roomPost.setDistrict(request.getDistrict());
        if (request.getCity() != null) roomPost.setCity(request.getCity());
        if (request.getElectricityPricePerKwh() != null) roomPost.setElectricityPricePerKwh(request.getElectricityPricePerKwh());
        if (request.getWaterPricePerM3() != null) roomPost.setWaterPricePerM3(request.getWaterPricePerM3());
        if (request.getInternetFee() != null) roomPost.setInternetFee(request.getInternetFee());
        if (request.getParkingFee() != null) roomPost.setParkingFee(request.getParkingFee());
        if (request.getCleaningFee() != null) roomPost.setCleaningFee(request.getCleaningFee());

        // Cập nhật ảnh chính nếu có upload mới
        if (mainImg != null && !mainImg.isEmpty()) {
            String url = fileService.saveImg(mainImg);
            roomPost.setMainImageUrl(url);
        }

        // Cập nhật gallery nếu có upload mới (thêm vào, không xóa cũ)
        if (gallery != null && !gallery.isEmpty()) {
            for (MultipartFile file : gallery) {
                if (!file.isEmpty()) {
                    RoomPostImages img = new RoomPostImages();
                    String url = fileService.saveImg(file);
                    img.setRoomPost(roomPost);
                    img.setImageUrl(url);
                    roomPost.getImages().add(img);
                }
            }
        }

        roomPost.setUpdatedAt(LocalDateTime.now());
        roomPostRepository.save(roomPost);
    }

}
