package com.omeralkan.applicationmicroservice.service;

import com.omeralkan.applicationmicroservice.repository.ErrorMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ErrorMessageService {

    private final ErrorMessageRepository errorMessageRepository;

    private static final String DEFAULT_LANGUAGE = "tr";
    private static final String FALLBACK_MESSAGE = "Beklenmeyen bir hata oluştu.";

    public String getMessage(String errorCode, String language) {
        String lang = (language == null || language.isBlank()) ? DEFAULT_LANGUAGE : language;

        return errorMessageRepository.findByErrorCodeAndLanguage(errorCode, lang)
                .map(entity -> entity.getMessage())
                .orElse(FALLBACK_MESSAGE);
    }
}