package vn.edu.ptit.dto.Request;

import lombok.Data;
import vn.edu.ptit.entity.Rooms;

@Data
public class AdminRoomStatusRequest {
    private Rooms.Status status;
}
