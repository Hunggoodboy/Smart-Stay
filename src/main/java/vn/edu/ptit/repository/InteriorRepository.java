package vn.edu.ptit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.ptit.entity.Interior;

import java.util.Optional;

@Repository
public interface InteriorRepository extends JpaRepository<Interior, Long> {
    Optional<Interior> findByName(String name);
}
