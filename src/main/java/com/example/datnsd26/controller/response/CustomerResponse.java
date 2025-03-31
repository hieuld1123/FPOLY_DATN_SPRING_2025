package com.example.datnsd26.controller.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CustomerResponse {
    private int id;

    private String maKhachHang;

    private String tenKhachHang;

    private String soDienThoai;

}
