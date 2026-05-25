package vn.edu.ptit.service;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import vn.edu.ptit.dto.Request.InteriorRoomRequest;
import vn.edu.ptit.dto.Response.ApiResponse;
import vn.edu.ptit.entity.Interior;
import vn.edu.ptit.entity.RoomInterior;
import vn.edu.ptit.entity.RoomPostImages;
import vn.edu.ptit.entity.RoomPosts;
import vn.edu.ptit.repository.InteriorRepository;
import vn.edu.ptit.repository.RoomInteriorRepository;
import vn.edu.ptit.repository.RoomPostImageRepository;
import vn.edu.ptit.repository.RoomPostRepository;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class InteriorService {
    private final InteriorRepository interiorRepository;
    private final RoomInteriorRepository roomInteriorRepository;
    private final RoomPostRepository roomPostRepository;
    private final RoomPostImageRepository  roomPostImageRepository;
    @Transactional
    public ApiResponse addInteriorRoom(InteriorRoomRequest request){
        Long roomId = request.getRoomId();
        List<String> interiorName = request.getInteriorName();
        for (String interior : interiorName) {
            Interior thisInterior = interiorRepository.findByName(interior)
                    .orElseGet(() -> {
                        Interior newInterior = new Interior();
                        newInterior.setName(interior);
                        return interiorRepository.save(newInterior);
                    });
            RoomInterior roomInterior = RoomInterior.builder()
                    .roomPost(roomPostRepository.findById(roomId).orElseThrow(() -> new RuntimeException("Chưa lưu room")))
                    .interior(thisInterior)
                    .build();
            roomInteriorRepository.save(roomInterior);
        }
        return ApiResponse.builder()
                .success(true)
                .message("Lưu nội thất thành công")
                .build();
    }
    public List<String> getInteriorsByRoomId(Long roomId) {
        return roomInteriorRepository.findByRoomPostId(roomId).stream()
                .map(roomInterior -> roomInterior.getInterior().getName())
                .toList();
    }

    @Transactional
    public ApiResponse addInteriorForAllRoomPosts() throws IOException {
        List<RoomPosts> roomPosts = roomPostRepository.findAll();
        for (RoomPosts roomPost : roomPosts) {
            Long roomPostId = roomPost.getId();
            List<String> imagesThisRoom = new ArrayList<>();
            List<String> images = roomPostImageRepository.getByRoomPostId(roomPostId).stream()
                    .map(image -> {
                        return image.getImageUrl();
                    })
                    .toList();
            imagesThisRoom.add(roomPost.getMainImageUrl());
            for(String image : images){
                imagesThisRoom.add(image);
            }
            try {
                MultiValueMap<String, Object> body = new LinkedMultiValueMap<>(); // ← khai báo ở đây
                for (String image : imagesThisRoom) {
                    byte[] imageBytes = null;
                    if (image.startsWith("http")){
                        imageBytes = new URI(image).toURL().openStream().readAllBytes();
                    }
                    else {
                        imageBytes = Files.readAllBytes(Paths.get("/Users/hunggoodboy/Downloads/smart-stay/src/main/resources/static/" + image));
                    }
                    body.add("file", new ByteArrayResource(imageBytes) {
                        @Override
                        public String getFilename() {
                            return "image.jpg";
                        }
                    });
                }
                HttpHeaders headers = new HttpHeaders();
                RestTemplate restTemplate = new RestTemplate();
                headers.setContentType(MediaType.MULTIPART_FORM_DATA);
                if (body.isEmpty()) {
                    System.out.println("SKIP room " + roomPostId + ": không có ảnh local");
                    continue;
                }
                ResponseEntity<Map> response = restTemplate.exchange(
                        "http://127.0.0.1:5000/predict",
                        HttpMethod.POST,
                        new HttpEntity<>(body, headers),
                        Map.class
                );
                List<String> amenities = (List<String>) response.getBody().get("detected_amenities");
                InteriorRoomRequest request = InteriorRoomRequest.builder()
                        .roomId(roomPostId)
                        .interiorName(amenities)
                        .build();
                ApiResponse responseCreateInterior = addInteriorRoom(request);
            }
            catch (Exception e) {
                System.err.println("Flask lỗi room " + roomPostId + ": " + e.getMessage());
            }
        }
        return ApiResponse.builder()
                .message("ok")
                .success(true)
                .build();
    }

//    @PostConstruct
//    public void init() throws IOException {
//        addInteriorForAllRoomPosts();
//    }
}
