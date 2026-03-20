package vn.edu.ptit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.ptit.entity.Notifications;

import java.util.List;

@Repository
public interface NotificationsRepository extends JpaRepository<Notifications, Long> {
    @Query("SELECT n from Notifications n join Rooms r On n.rooms.id = r.id join Contracts c On r.id = c.room.id where c.customer.id = :customerId")
    List<Notifications> findAllByCustomerId(@Param("customerId") Long customerId);
}
