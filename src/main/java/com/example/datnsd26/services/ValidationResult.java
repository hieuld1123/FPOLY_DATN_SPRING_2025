package com.example.datnsd26.services;

import lombok.Data;
import java.util.HashMap;
import java.util.Map;

@Data
public class ValidationResult {
    private Map<String, String> errors = new HashMap<>();

    public void addError(String field, String message) {
        errors.put(field, message);
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }
}
