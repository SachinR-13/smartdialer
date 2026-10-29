package com.smartdialer.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity @Table(name="agents", indexes=@Index(name="idx_agent_status", columnList="status"))
public class Agent {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false) private String name;
    @Enumerated(EnumType.STRING) @Column(nullable=false, length=24) private AgentStatus status=AgentStatus.AVAILABLE;
    private Instant reservedAt;
    @Version private long version;
    protected Agent() {}
    public Agent(String name, AgentStatus status) { this.name=name; this.status=status; }
    public Long getId(){return id;} public String getName(){return name;} public AgentStatus getStatus(){return status;} public Instant getReservedAt(){return reservedAt;} public long getVersion(){return version;}
    public void setStatus(AgentStatus status){this.status=status; if(status==AgentStatus.RESERVED) this.reservedAt=Instant.now(); else if(status==AgentStatus.AVAILABLE || status==AgentStatus.OFFLINE || status==AgentStatus.PAUSED) this.reservedAt=null;}
}
