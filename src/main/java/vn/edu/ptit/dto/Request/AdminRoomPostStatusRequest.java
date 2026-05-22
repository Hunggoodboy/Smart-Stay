package vn.edu.ptit.dto.Request;

import lombok.Data;
import vn.edu.ptit.entity.RoomPosts;

@Data
public class AdminRoomPostStatusRequest {
    private RoomPosts.Status status;
}
