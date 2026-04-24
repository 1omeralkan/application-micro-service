package com.omeralkan.applicationmicroservice.entity;

public enum ApplicationStatus {
    PENDING,     // Beklemede
    APPROVED,    // Onaylandı
    REJECTED,    // Reddedildi
    CANCELLED    // İptal edildi
}