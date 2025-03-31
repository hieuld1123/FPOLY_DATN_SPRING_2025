package com.example.datnsd26.services;

import java.util.Map;

public class VoucherValidationException extends RuntimeException {

    private Map<String, String> errors;

    public VoucherValidationException(Map<String, String> errors) {
        this.errors = errors;
    }

    public Map<String, String> getErrors() {
        return errors;
    }
}
