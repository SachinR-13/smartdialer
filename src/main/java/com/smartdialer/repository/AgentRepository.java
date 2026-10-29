package com.smartdialer.repository;
import com.smartdialer.domain.*; import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param; import jakarta.persistence.LockModeType; import java.util.*;
public interface AgentRepository extends JpaRepository<Agent,Long> {
 long countByStatus(AgentStatus status);
 @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select a from Agent a where a.status=:status order by a.id") List<Agent> lockByStatus(@Param("status") AgentStatus status, org.springframework.data.domain.Pageable pageable);
}
