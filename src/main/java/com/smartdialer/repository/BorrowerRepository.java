package com.smartdialer.repository;
import com.smartdialer.domain.*; import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param; import jakarta.persistence.LockModeType; import java.util.*;
public interface BorrowerRepository extends JpaRepository<Borrower,Long> {
 @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select b from Borrower b where b.status=:status order by b.id") List<Borrower> lockByStatus(@Param("status") BorrowerStatus status, org.springframework.data.domain.Pageable pageable);
}
