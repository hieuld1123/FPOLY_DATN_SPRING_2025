package com.example.datnsd26.controller.request;

import lombok.Getter;

@Getter
public class StoreCustomerRequest {
    private String recipient_name;

    private String phone_number;

    private String email;

    private String province;

    private String district;

    private String ward;

    private String addressDetail;
}
