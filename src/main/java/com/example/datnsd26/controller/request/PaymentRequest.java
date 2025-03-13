package com.example.datnsd26.controller.request;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaymentRequest {
    private int invoiceId;
    private int customerId;
    private int employeeId;
    private String type;
}
