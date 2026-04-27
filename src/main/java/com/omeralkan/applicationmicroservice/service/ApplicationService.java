package com.omeralkan.applicationmicroservice.service;

import com.omeralkan.applicationmicroservice.dto.request.ApplicationRequestDto;
import com.omeralkan.applicationmicroservice.dto.response.ApplicationResponseDto;

import java.util.List;

public interface ApplicationService {

    ApplicationResponseDto createApplication(ApplicationRequestDto requestDto);

    ApplicationResponseDto getApplicationById(Long id);

    ApplicationResponseDto getApplicationByNumber(String applicationNumber);

    List<ApplicationResponseDto> getAllApplications();

    List<ApplicationResponseDto> getApplicationsByCustomerId(Long customerId);

    ApplicationResponseDto updateApplicationStatus(Long id, String status);

    void deleteApplication(Long id);
}