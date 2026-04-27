package com.omeralkan.applicationmicroservice.controller;

import com.omeralkan.applicationmicroservice.dto.request.ApplicationRequestDto;
import com.omeralkan.applicationmicroservice.dto.response.ApplicationResponseDto;
import com.omeralkan.applicationmicroservice.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    @PostMapping
    public ResponseEntity<ApplicationResponseDto> createApplication(
            @RequestBody ApplicationRequestDto requestDto) {

        ApplicationResponseDto response = applicationService.createApplication(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApplicationResponseDto> getApplicationById(@PathVariable Long id) {

        ApplicationResponseDto response = applicationService.getApplicationById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/number/{applicationNumber}")
    public ResponseEntity<ApplicationResponseDto> getApplicationByNumber(
            @PathVariable String applicationNumber) {

        ApplicationResponseDto response = applicationService.getApplicationByNumber(applicationNumber);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<ApplicationResponseDto>> getAllApplications() {

        List<ApplicationResponseDto> responses = applicationService.getAllApplications();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<ApplicationResponseDto>> getApplicationsByCustomerId(
            @PathVariable Long customerId) {

        List<ApplicationResponseDto> responses = applicationService.getApplicationsByCustomerId(customerId);
        return ResponseEntity.ok(responses);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApplicationResponseDto> updateApplicationStatus(
            @PathVariable Long id,
            @RequestParam String status) {

        ApplicationResponseDto response = applicationService.updateApplicationStatus(id, status);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApplication(@PathVariable Long id) {

        applicationService.deleteApplication(id);
        return ResponseEntity.noContent().build();
    }
}