package vn.edu.ptit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.ptit.entity.Appointments;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointments, Long> {

    List<Appointments> findByCustomerId(Long customerId);

    List<Appointments> findByLandlordId(Long landlordId);

    Optional<Appointments> findByRentalRequestId(Long rentalRequestId);

    boolean existsByRentalRequestId(Long rentalRequestId);
}
