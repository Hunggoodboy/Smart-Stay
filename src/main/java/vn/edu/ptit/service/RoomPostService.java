package vn.edu.ptit.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import vn.edu.ptit.dto.Request.CreateRoomPostRequest;
import vn.edu.ptit.dto.Response.ApiResponse;
import vn.edu.ptit.entity.LandLord;
import vn.edu.ptit.entity.RoomPostImages;
import vn.edu.ptit.entity.RoomPosts;
import vn.edu.ptit.entity.User;
import vn.edu.ptit.repository.LandLordRepository;
import vn.edu.ptit.repository.RoomPostImageRepository;
import vn.edu.ptit.repository.RoomPostRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CreateRoomPostService {
    private final RoomPostRepository roomPostRepository;
    private final AuthService  authService;
    private final LandLordRepository  landLordRepository;
    private final RoomPostImageRepository  roomPostImageRepository;
    public ApiResponse createNewRoomPost(CreateRoomPostRequest createRoomPostRequest) {
        RoomPosts roomPosts = new RoomPosts();
        BeanUtils.copyProperties(createRoomPostRequest, roomPosts);
       Long userId = authService.getCurrentUserId();
        LandLord landLord = landLordRepository.getReferenceById(userId);
        roomPosts.setLandLord(landLord);
        roomPosts.setStatus(RoomPosts.Status.ACTIVE);
        roomPosts.setMainImageUrl(createRoomPostRequest.getMainImageUrl());
        List<String> images = createRoomPostRequest.getImageUrl();
        for(String image : images) {
            RoomPostImages  roomPostImage = new RoomPostImages();
            roomPostImage.setRoomPost(roomPosts);
            roomPostImage.setImageUrl(image);
            roomPostImageRepository.save(roomPostImage);
        }

    }
}
