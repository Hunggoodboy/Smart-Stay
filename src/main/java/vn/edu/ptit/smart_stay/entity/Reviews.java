package vn.edu.ptit.smart_stay.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@ToString
@SuperBuilder
@NoArgsConstructor
@Table(name = "reviews")
public class Reviews implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "room_id", nullable = false)
    private Long roomId;

    @Column(name = "rating", nullable = false)
    private Long rating;

    @Column(name = "comment")
    private String comment;

    @Column(name = "cleanliness_rating")
    private Long cleanlinessRating;

    @Column(name = "location_rating")
    private Long locationRating;

    @Column(name = "price_rating")
    private Long priceRating;

    @Column(name = "landlord_rating")
    private Long landlordRating;

    @Column(name = "is_anonymous", nullable = false)
    private Boolean anonymous = Boolean.FALSE;

    @Column(name = "is_approved", nullable = false)
    private Boolean approved = Boolean.FALSE;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

}
