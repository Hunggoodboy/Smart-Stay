package vn.edu.ptit.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.ptit.entity.Rooms;

@Repository
public interface RoomsRepository extends JpaRepository<Rooms, Long> {
}
