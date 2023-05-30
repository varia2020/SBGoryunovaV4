package com.example.springsecurityapplication.enumm;

import org.springframework.stereotype.Component;
import org.springframework.core.convert.converter.Converter;

@Component
public class StatusConverter implements Converter<String, Status> {

    @Override
    public Status convert(String source) {
        try {
            return Status.valueOf(source.toUpperCase());
        } catch (IllegalArgumentException e) {
            // Обработка недопустимого значения статуса
            return null; // Или выбросить исключение, если необходимо
        }
    }
}
