package vn.edu.ptit.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.ptit.dto.Response.ApiResponse;
import vn.edu.ptit.service.AI.VectorService;

@RestController
@RequestMapping("/api/vector")
@AllArgsConstructor
public class VectorController {
    private final VectorService vectorService;

    @PostMapping("/generate-all-room-posts")
    public ApiResponse generateAll() {
        vectorService.addAllVectorsRoomPosts();
        return new ApiResponse("Vectors generated successfully", true);
    }
}
