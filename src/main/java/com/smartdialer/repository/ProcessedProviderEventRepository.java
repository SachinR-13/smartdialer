package com.smartdialer.repository;
import com.smartdialer.domain.ProcessedProviderEvent; import org.springframework.data.jpa.repository.JpaRepository;
public interface ProcessedProviderEventRepository extends JpaRepository<ProcessedProviderEvent,String> {}
