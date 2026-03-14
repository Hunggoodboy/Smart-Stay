package vn.edu.ptit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.ptit.entity.Admin;

@Repository
public interface AdminsRepository extends JpaRepository<Admin, Long> {

}
