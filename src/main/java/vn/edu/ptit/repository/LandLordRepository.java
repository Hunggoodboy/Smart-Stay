package vn.edu.ptit.repository;

import org.springframework.stereotype.Repository;
import vn.edu.ptit.entity.LandLord;

@Repository
public interface LandLordRepository {
    public LandLord findLandLordById(Long id);
}
