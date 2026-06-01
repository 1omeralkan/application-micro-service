package com.omeralkan.applicationmicroservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "application_risk_profiles")
public class ApplicationRiskProfileEntity extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false, unique = true)
    private ApplicationEntity application;

    @Column(name = "age", nullable = false)
    private Integer age;

    @Column(name = "height", nullable = false, precision = 5, scale = 2)
    private BigDecimal height;

    @Column(name = "weight", nullable = false, precision = 5, scale = 2)
    private BigDecimal weight;

    @Column(name = "gender", nullable = false, length = 1)
    private String gender;
}