package vn.edu.ptit.service.AI;

import lombok.AllArgsConstructor;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import vn.edu.ptit.dto.Response.ApiResponse;
import vn.edu.ptit.entity.RoomPosts;
import vn.edu.ptit.repository.RoomPostRepository;
import org.springframework.ai.document.Document;


import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class VectorService {
    private final DocumentService documentService;
    private final RoomPostRepository roomPostRepository;
    private final PgVectorStore vectorStore;

    public ApiResponse addAllVectorsRoomPosts(){
        int pageNumber = 0;
        int pageSize = 100;
        Page<RoomPosts> page;
        do{
            Pageable pageable =  PageRequest.of(pageNumber, pageSize, Sort.by("id").ascending());
            page = roomPostRepository.findAll(pageable);
            List<RoomPosts> roomPostsList = page.getContent();
            if(roomPostsList.isEmpty()) {
                break;
            }
            else{
                List<Document> documentsPostRooms = new ArrayList<>();
                for (RoomPosts roomPosts : roomPostsList) {
                    documentsPostRooms.add(documentService.convertRoomPostToDocument(roomPosts));
                }
                vectorStore.add(documentsPostRooms);
                pageNumber++;
            }
        }
        while (page.hasNext());
        return ApiResponse.builder()
                          .success(true)
                          .message("Đã thêm tất cả vector của phòng trọ vào cơ sở dữ liệu")
                          .build();
    }

    public ApiResponse addRoomPosts(RoomPosts roomPosts){
        vectorStore.add(List.of(documentService.convertRoomPostToDocument(roomPosts)));
        return ApiResponse.builder()
                          .success(true)
                          .message("Đã thêm vector của phòng này vào cơ sở dữ liệu")
                          .build();
    }
}
