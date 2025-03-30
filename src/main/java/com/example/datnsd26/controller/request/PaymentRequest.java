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
    private String recipient_name;
    private String phone_number;
    private String email;
    private String province;
    private String district;
    private String ward;
    private String addressDetail;
    private String paymentMethod;
}
