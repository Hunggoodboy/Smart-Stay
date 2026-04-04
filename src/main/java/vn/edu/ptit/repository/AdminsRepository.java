package vn.edu.ptit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.ptit.entity.Admins;

@Repository
public interface AdminsRepository extends JpaRepository<Admins, Long> {
    Admins findByEmail(String email);
}
