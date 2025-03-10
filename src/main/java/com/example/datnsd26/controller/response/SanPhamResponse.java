package com.example.datnsd26.controller.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SanPhamResponse {
    private int id; // id product detail

    private String tenSanPham;

    private String maSanPham;

    private String hinhAnh;

    private int soLuong;

    private int soLuongTonKho;

    private float gia;
}
