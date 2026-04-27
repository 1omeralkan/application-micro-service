package com.omeralkan.applicationmicroservice.repository;

import com.omeralkan.applicationmicroservice.entity.ErrorMessageEntity;
import com.omeralkan.applicationmicroservice.entity.ErrorMessageId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ErrorMessageRepository extends JpaRepository<ErrorMessageEntity, ErrorMessageId> {

    Optional<ErrorMessageEntity> findByErrorCodeAndLanguage(String errorCode, String language);
}