package vn.edu.ptit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.ptit.entity.Users;



@Repository
public interface UsersRepository extends JpaRepository<Users, Long> {

}