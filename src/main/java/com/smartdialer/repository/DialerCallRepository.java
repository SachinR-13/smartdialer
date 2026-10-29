
package com.smartdialer.repository;

import com.smartdialer.domain.CallStatus;
import com.smartdialer.domain.DialerCall;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DialerCallRepository extends JpaRepository<DialerCall, Long> {

    List<DialerCall> findTop100ByOrderByCreatedAtDesc();

    long countByStatusIn(List<CallStatus> statuses);

    @Query("""
        SELECT c FROM DialerCall c
        JOIN FETCH c.agent
        JOIN FETCH c.borrower
        ORDER BY c.createdAt DESC
        """)
    List<DialerCall> findAllWithAgentAndBorrower(Pageable pageable);

    @Query("""
        SELECT c FROM DialerCall c
        JOIN FETCH c.agent
        JOIN FETCH c.borrower
        WHERE c.id = :id
        """)
    Optional<DialerCall> findByIdWithAgentAndBorrower(@Param("id") Long id);
}
