package vn.edu.ptit.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.edu.ptit.entity.Contracts;

@Repository
public interface ContractsRepository extends JpaRepository<Contracts, Long> {
    Optional<Contracts> findByRoomIdAndStatus(Long roomId, String status);
}