package com.smartdialer.domain;

import jakarta.persistence.*;

@Entity @Table(name="borrowers", indexes=@Index(name="idx_borrower_status", columnList="status"))
public class Borrower {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false) private String name;
    @Column(nullable=false) private String phoneNumber;
    @Enumerated(EnumType.STRING) @Column(nullable=false, length=24) private BorrowerStatus status=BorrowerStatus.QUEUED;
    @Version private long version;
    protected Borrower() {}
    public Borrower(String name, String phoneNumber) { this.name=name; this.phoneNumber=phoneNumber; }
    public Long getId(){return id;} public String getName(){return name;} public String getPhoneNumber(){return phoneNumber;} public BorrowerStatus getStatus(){return status;} public long getVersion(){return version;}
    public void setStatus(BorrowerStatus status){this.status=status;}
}
