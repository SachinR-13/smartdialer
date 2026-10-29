package com.smartdialer.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity @Table(name="dialer_calls", indexes={@Index(name="idx_call_status", columnList="status"), @Index(name="idx_call_provider_id", columnList="providerCallId")})
public class DialerCall {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional=false, fetch=FetchType.LAZY) private Agent agent;
    @ManyToOne(optional=false, fetch=FetchType.LAZY) private Borrower borrower;
    @Enumerated(EnumType.STRING) @Column(nullable=false, length=24) private CallStatus status=CallStatus.QUEUED;
    @Column(nullable=false, unique=true) private String idempotencyKey;
    private String providerCallId;
    @Column(nullable=false) private String providerName;
    @Column(nullable=false) private Instant createdAt=Instant.now();
    private Instant updatedAt=Instant.now();
    @Version private long version;
    protected DialerCall() {}
    public DialerCall(Agent agent, Borrower borrower, String key, String providerName) { this.agent=agent; this.borrower=borrower; this.idempotencyKey=key; this.providerName=providerName; this.status=CallStatus.RESERVED; }
    public Long getId(){return id;} public Agent getAgent(){return agent;} public Borrower getBorrower(){return borrower;} public CallStatus getStatus(){return status;} public String getIdempotencyKey(){return idempotencyKey;} public String getProviderCallId(){return providerCallId;} public String getProviderName(){return providerName;} public Instant getCreatedAt(){return createdAt;} public Instant getUpdatedAt(){return updatedAt;} public long getVersion(){return version;}
    public void setStatus(CallStatus status){this.status=status; this.updatedAt=Instant.now();}
    public void setProviderCallId(String id){this.providerCallId=id; this.updatedAt=Instant.now();}
}
