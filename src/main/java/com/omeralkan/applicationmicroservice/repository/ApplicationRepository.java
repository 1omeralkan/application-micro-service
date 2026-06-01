package com.omeralkan.applicationmicroservice.repository;

import com.omeralkan.applicationmicroservice.entity.ApplicationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<ApplicationEntity, Long> {

    List<ApplicationEntity> findAllByIsActiveTrue();

    List<ApplicationEntity> findAllByCustomerIdAndIsActiveTrue(Long customerId);

    Optional<ApplicationEntity> findByApplicationNumberAndIsActiveTrue(String applicationNumber);

    Optional<ApplicationEntity> findTopByApplicationNumberStartingWithOrderByIdDesc(String prefix);
}