package com.example.datnsd26.controller.request;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class InvoiceRecipientInfoRequest {
    private String name;
    private String phone;
    private String province;
    private String district;
    private String ward;
    private String specificAddress;
}
