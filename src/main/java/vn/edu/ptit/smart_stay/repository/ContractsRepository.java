package vn.edu.ptit.smart_stay.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import vn.edu.ptit.smart_stay.entity.Contracts;

public interface ContractsRepository extends JpaRepository<Contracts, Long>, JpaSpecificationExecutor<Contracts> {

}