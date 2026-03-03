package vn.edu.ptit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.ptit.entity.Notifications;

@Repository
public interface NotificationsRepository extends JpaRepository<Notifications, Long> {
}
