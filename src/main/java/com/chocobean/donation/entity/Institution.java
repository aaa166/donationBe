package com.chocobean.donation.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "institution")
public class Institution {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long institutionNo;
    @Column(nullable = false, length = 100)
    private String institutionName;
    @Column(nullable = false)
    private String institutionUrl;
    @Column(nullable = false)
    private String institutionTel;

}
