package vn.edu.ptit.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.ptit.dto.Request.CreateRoomPostRequest;
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
    public ResponseEntity<ApiResponse> createNewPostRoom(@RequestPart("request") CreateRoomPostRequest  createRoomPostRequest,
                                                         @RequestPart(value = "mainImage", required = false) MultipartFile mainImg,
                                                         @RequestPart(value = "gallery", required = false) List<MultipartFile> images) throws IOException {
        return ResponseEntity.ok(roomPostService.createNewRoomPost(createRoomPostRequest, mainImg, images));
    }

    @GetMapping("/room-posted")
    private ResponseEntity<List<RoomPostSummaryResponse> >  getRoomPosted(){
        return ResponseEntity.ok(roomPostService.getAllRoomPosts());
    }

    @GetMapping("/room-detail/{id}")
    private ResponseEntity<RoomPostDetailResponse> getRoomPostById(@PathVariable Long id){
        return ResponseEntity.ok(roomPostService.getRoomPostDetail(id));
    }
}
