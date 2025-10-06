package org.kergru.library.loans.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LoanRepository extends JpaRepository<LoanEntity, Long> {

  @Query("SELECT l FROM LoanEntity l JOIN FETCH l.book WHERE l.userId = :userId")
  List<LoanEntity> findByUserId(@Param("userId") Long userId);

}

