package com.omeralkan.applicationmicroservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "applications")
public class ApplicationEntity extends BaseEntity {

    @Column(name = "application_number", nullable = false, unique = true, length = 50)
    private String applicationNumber;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "product_amount_id", nullable = false)
    private Long productAmountId;

    @Column(name = "application_date", nullable = false)
    private LocalDate applicationDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ApplicationStatus status;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "payment_type_code", nullable = false, length = 20)
    private String paymentTypeCode;

    @Column(name = "installment_count", nullable = false)
    private Integer installmentCount;

    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private java.util.List<ApplicationCoverageEntity> coverages = new java.util.ArrayList<>();

    @Column(name = "is_health_report_required", nullable = false)
    private Boolean isHealthReportRequired = false;

    @OneToOne(mappedBy = "application", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private ApplicationRiskProfileEntity riskProfile;
}