package vn.edu.ptit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.ptit.entity.Customers;

@Repository
public interface CustomersRepository extends JpaRepository<Customers, Long> {
}
