package com.example.datnsd26.controller.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class HoaDonChiTietResponse {
    private float tongTien;

    private String ghiChu;

    private List<SanPhamResponse> listSanPham;
}
