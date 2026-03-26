package vn.edu.ptit.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Ảnh gallery của bài đăng cho thuê phòng.
 * Ảnh đại diện được đánh dấu bằng isThumbnail = true.
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

    // ==================== RELATIONSHIPS ====================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_post_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private RoomPosts roomPost;
}
