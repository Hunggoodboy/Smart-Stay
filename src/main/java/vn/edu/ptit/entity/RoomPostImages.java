package vn.edu.ptit.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Ảnh của bài đăng cho thuê phòng.
 * Một bài đăng có thể có nhiều ảnh; ảnh đại diện được đánh dấu bằng isThumbnail.
 */
@Entity
@Data
@Table(name = "room_post_images")
public class RoomPostImages implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    /**
     * Thứ tự hiển thị (0 = đầu tiên)
     */
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;

    /**
     * Đánh dấu ảnh đại diện (thumbnail) cho bài đăng
     */
    @Column(name = "is_thumbnail", nullable = false)
    private Boolean thumbnail = false;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // ==================== RELATIONSHIPS ====================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_post_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private RoomPosts roomPost;
}
