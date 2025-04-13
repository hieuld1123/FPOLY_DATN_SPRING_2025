package com.example.datnsd26.controller.response;

import lombok.Builder;
import lombok.Getter;

import java.util.Date;
import java.util.List;

@Getter
@Builder
public class HoaDonChiTietResponse {
    private Integer id;

    private float tongTien;

    private float shippingFee;

    private String ghiChu;

    private Customer khachHang;

    private String seller;

    private Date ngayTao;

    private Date ngayCapNhat;

    private List<SanPhamResponse> listSanPham;

    @Getter
    @Builder
    public static class Customer{
        private int id;

        private String maKhachHang;

        private String tenKhachHang;

        private String soDienThoai;

        private String diaChi;
    }
}
