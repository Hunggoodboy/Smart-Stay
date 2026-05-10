package vn.edu.ptit.service.AI;

import lombok.AllArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;
import vn.edu.ptit.entity.RoomPosts;

@Service
@AllArgsConstructor
public class DocumentService {
    public Document convertRoomPostToDocument(RoomPosts roomPosts){
        return Document.builder()
                .metadata("roomId", roomPosts.getId().toString())
                .metadata("title", roomPosts.getTitle())
                .metadata("description", roomPosts.getDescription())
                .metadata("ward", roomPosts.getWard())
                .metadata("district", roomPosts.getDistrict())
                .metadata("city", roomPosts.getCity())
                .metadata("address", roomPosts.getAddress())
                .metadata("price", roomPosts.getMonthlyRent().toString())
                .metadata("area", roomPosts.getAreaM2().toString())
                .metadata("roomType", roomPosts.getRoomType())
                .text(String.format("""
                        Phòng trọ này có tiêu đề là: %s,
                        Mô tả về phòng trọ này là : %s,
                        Phòng trọ này nằm ở phường: %s, quận: %s, thành phố: %s, địa chỉ cụ thể là: %s,
                        Phòng trọ này có giá thuê hàng tháng là: %s,
                        Diện tích của phòng trọ này là: %s m2,
                        Loại phòng trọ này là: %s
                        """, roomPosts.getTitle(), roomPosts.getDescription(), roomPosts.getWard(), roomPosts.getDistrict(),
                        roomPosts.getCity(), roomPosts.getAddress(), roomPosts.getMonthlyRent().toString(),
                        roomPosts.getAreaM2().toString(), roomPosts.getRoomType()))
                .build();
    }

}
