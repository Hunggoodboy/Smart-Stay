package vn.edu.ptit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.edu.ptit.entity.Contracts;
import vn.edu.ptit.entity.LandLord;

import java.util.Optional;

@Repository
public interface ContractsRepository extends JpaRepository<Contracts, Long> {
}
