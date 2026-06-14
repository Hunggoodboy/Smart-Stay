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


    @Modifying(clearAutomatically = true)
    @Transactional
    @Query(value = "UPDATE users SET user_type = :userType where id = :userId", nativeQuery = true)
    void updateUserType(@Param("userType") String userType, @Param("userId") Long userId);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query(value = "insert into customers(user_id, address,id_card_number,is_verified) values (:userId, :address, :idCardNumber, true)", nativeQuery = true)
    void insertIntoCustomer(@Param("userId") Long userId, @Param("address") String address, @Param("idCardNumber") String idCardNumber);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query(value = "insert into landlords(user_id, address, id_card_number, is_verified) values (:userId, :address, :idCardNumber, false)", nativeQuery = true)
    void insertIntoLandLord(@Param("userId") Long userId, @Param("address") String address, @Param("idCardNumber") String idCardNumber);

    @Query(value = "SELECT is_verified FROM landlords WHERE user_id = :userId", nativeQuery = true)
    Boolean getLandlordVerificationStatus(@Param("userId") Long userId);
}
