package com.example.datnsd26.controller.request;

import lombok.Getter;

@Getter
public class AddressRequest {
    private String ward;

    private String province;

    private String district;

    private String addressDetail;
}
