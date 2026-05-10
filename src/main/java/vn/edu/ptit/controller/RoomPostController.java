package vn.edu.ptit.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.ptit.dto.Request.CreateRoomPostRequest;
import vn.edu.ptit.dto.Request.RentalRequestDTO;
import vn.edu.ptit.dto.Response.ApiResponse;
import vn.edu.ptit.dto.Response.RoomPostDetailResponse;
import vn.edu.ptit.dto.Response.RoomPostSummaryResponse;
import vn.edu.ptit.service.RoomPostService;

import java.io.IOException;
import java.util.List;

@RestController
@AllArgsConstructor
@CrossOrigin(origins = "*")
public class RoomPostController {
    private final RoomPostService roomPostService;

    @PostMapping("/api/post-room")
    public ResponseEntity<ApiResponse> createNewPostRoom(
            @RequestPart("request") CreateRoomPostRequest createRoomPostRequest,
            @RequestPart(value = "mainImage", required = false) MultipartFile mainImg,
            @RequestPart(value = "gallery", required = false) List<MultipartFile> images) throws IOException {
        return ResponseEntity.ok(roomPostService.createNewRoomPost(createRoomPostRequest, mainImg, images));
    }

    @PostMapping("/api/post-list-room")
    public ResponseEntity<?> createNewPostListRoom(
            // Dùng @RequestBody để hứng toàn bộ mảng JSON
            @RequestBody @Valid List<CreateRoomPostRequest> requestList
    ) {
        int successCount = 0;

        for (CreateRoomPostRequest request : requestList) {
            try {
                // Truyền null cho 2 tham số MultipartFile vì ta dùng link ảnh từ HTTP đã có sẵn trong 'request'
                roomPostService.createNewRoomPost(request, null, null);
                successCount++;
            } catch (Exception e) {
                System.err.println("Lỗi khi lưu bài đăng " + request.getTitle() + ": " + e.getMessage());
            }
        }

        // Trả về response tùy theo cấu trúc ApiResponse của dự án
        return ResponseEntity.ok("Đã tạo thành công " + successCount + "/" + requestList.size() + " bài đăng!");
    }

    @GetMapping("/room-posted")
    private ResponseEntity<List<RoomPostSummaryResponse>> getRoomPosted() {
        return ResponseEntity.ok(roomPostService.getAllRoomPosts());
    }

    @GetMapping("/room-detail/{id}")
    private ResponseEntity<RoomPostDetailResponse> getRoomPostById(@PathVariable Long id) {
        return ResponseEntity.ok(roomPostService.getRoomPostDetail(id));
    }

    @GetMapping("/api/landlord/posts")
    public ResponseEntity<List<RoomPostSummaryResponse>> getPostsForLandlord() {
        return ResponseEntity.ok(roomPostService.getPostsForCurrentLandlord());
    }

}
