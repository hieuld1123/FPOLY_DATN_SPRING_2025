package com.example.datnsd26.controller.response;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class CustomerAddressResponse {
    private String province;

    private String district;

    private String ward;

    private String addressDetail;
}
