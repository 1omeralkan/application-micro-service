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
@Table(name = "application_coverages", uniqueConstraints = {
        @UniqueConstraint(name = "uq_app_coverage_code", columnNames = {"application_id", "coverage_code"})
})
public class ApplicationCoverageEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private ApplicationEntity application;

    @Column(name = "coverage_code", nullable = false, length = 50)
    private String coverageCode;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    // Hesaplanmış net teminat tutarı
    @Column(name = "calculated_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal calculatedAmount;
}