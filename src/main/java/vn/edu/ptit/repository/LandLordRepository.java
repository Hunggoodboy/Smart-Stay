package vn.edu.ptit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.ptit.entity.LandLord;

import java.util.Optional;

@Repository
public interface LandLordRepository extends JpaRepository<LandLord, Long> {
    Optional<LandLord> findLandLordById(Long userId);
    LandLord getReferenceById(Long userId);
}
