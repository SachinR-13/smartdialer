package com.smartdialer.domain;
import jakarta.persistence.*;
import java.time.Instant;
@Entity @Table(name="processed_provider_events")
public class ProcessedProviderEvent {
 @Id @Column(length=160) private String eventId;
 @Column(nullable=false) private Instant processedAt=Instant.now();
 protected ProcessedProviderEvent() {}
 public ProcessedProviderEvent(String eventId){this.eventId=eventId;}
}
