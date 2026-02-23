package vn.edu.ptit.smart_stay.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import vn.edu.ptit.smart_stay.entity.Admins;

public interface AdminsRepository extends JpaRepository<Admins, Long>, JpaSpecificationExecutor<Admins> {

}