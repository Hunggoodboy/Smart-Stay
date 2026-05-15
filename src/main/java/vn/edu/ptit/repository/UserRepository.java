package vn.edu.ptit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.ptit.entity.User;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    @Query("SELECT u.fullName FROM User u WHERE u.id = :id")
    String findFullNameById(@Param("id") Long id);
    Optional<User> findByEmail(String email);

    @Modifying
    @Transactional
    @Query(value = "UPDATE users SET user_type = 'TENANT' where id = :userId", nativeQuery = true)
    void updateUserType(@Param("userId") Long userId);

    @Modifying
    @Transactional
    @Query(value = "insert into customers(user_id, address,id_card_number,is_verified) values (:userId, :address, :idCardNumber, true)", nativeQuery = true)
    void insertIntoCustomer(@Param("userId") Long userId, @Param("address") String address, @Param("idCardNumber") String idCardNumber);
}
