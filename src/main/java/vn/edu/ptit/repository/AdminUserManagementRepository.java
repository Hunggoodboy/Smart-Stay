package vn.edu.ptit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.ptit.entity.User;

import java.util.List;

@Repository
public interface AdminUserManagementRepository extends JpaRepository<User, Long> {

    @Query("""
            SELECT u
            FROM User u
            WHERE (:includeDeleted = true OR u.deletedAt IS NULL)
            ORDER BY u.createdAt DESC, u.id DESC
            """)
    List<User> findAllForAdmin(@Param("includeDeleted") boolean includeDeleted);

    @Query("""
            SELECT u
            FROM User u
            WHERE (:includeDeleted = true OR u.deletedAt IS NULL)
              AND u.role = :role
            ORDER BY u.createdAt DESC, u.id DESC
            """)
    List<User> findByRoleForAdmin(
            @Param("role") User.Role role,
            @Param("includeDeleted") boolean includeDeleted
    );

    @Query("""
            SELECT u
            FROM User u
            WHERE (:includeDeleted = true OR u.deletedAt IS NULL)
              AND u.active = :active
            ORDER BY u.createdAt DESC, u.id DESC
            """)
    List<User> findByStatusForAdmin(
            @Param("active") Boolean active,
            @Param("includeDeleted") boolean includeDeleted
    );

    @Query("""
            SELECT u
            FROM User u
            WHERE (:includeDeleted = true OR u.deletedAt IS NULL)
              AND (LOWER(COALESCE(u.fullName, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(COALESCE(u.email, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(COALESCE(u.username, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(COALESCE(u.phoneNumber, '')) LIKE LOWER(CONCAT('%', :keyword, '%')))
            ORDER BY u.createdAt DESC, u.id DESC
            """)
    List<User> findByKeywordForAdmin(
            @Param("keyword") String keyword,
            @Param("includeDeleted") boolean includeDeleted
    );

    @Query("""
            SELECT COUNT(u)
            FROM User u
            WHERE u.active = false
              AND u.deletedAt IS NULL
            """)
    long countLockedUsers();
}
