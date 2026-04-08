package vn.edu.ptit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.ptit.entity.User;



@Repository
public interface UsersRepository extends JpaRepository<User, Long> {

}