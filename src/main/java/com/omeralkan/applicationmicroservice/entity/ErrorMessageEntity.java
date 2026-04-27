package com.omeralkan.applicationmicroservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "error_messages")
@IdClass(ErrorMessageId.class)
public class ErrorMessageEntity {

    @Id
    @Column(name = "error_code", nullable = false, length = 50)
    private String errorCode;

    @Id
    @Column(name = "language", nullable = false, length = 5)
    private String language;

    @Column(name = "message", nullable = false, length = 500)
    private String message;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by", nullable = false, updatable = false)
    private String createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private String updatedBy;
}