package vn.edu.ptit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.ptit.entity.RentPayments;

import java.util.List;
import java.util.Optional;

@Repository
public interface LandlordRentPaymentRepository extends JpaRepository<RentPayments, Long> {

    @Query(value = """
            select p.*
            from rent_payments p
            join contracts c on p.contract_id = c.id
            where c.landlord_id = :landlordId
            and (:status is null or p.status = :status)
            order by p.due_date asc, p.created_at desc
            """, nativeQuery = true)
    List<RentPayments> findAllForLandlord(
            @Param("landlordId") Long landlordId,
            @Param("status") String status
    );

    @Query(value = """
            select p.*
            from rent_payments p
            join contracts c on p.contract_id = c.id
            where c.landlord_id = :landlordId
            and p.status <> :paidStatus
            order by p.due_date asc, p.created_at desc
            """, nativeQuery = true)
    List<RentPayments> findUnpaidForLandlord(
            @Param("landlordId") Long landlordId,
            @Param("paidStatus") String paidStatus
    );

    @Query(value = """
            select p.*
            from rent_payments p
            join contracts c on p.contract_id = c.id
            where p.id = :paymentId
            and c.landlord_id = :landlordId
            """, nativeQuery = true)
    Optional<RentPayments> findForLandlordById(
            @Param("paymentId") Long paymentId,
            @Param("landlordId") Long landlordId
    );
}
