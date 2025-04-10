package com.example.datnsd26.controller.response;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class PublicSanPhamResponse {
    private Integer id;

    private String tenSanPham;

    private String hinhAnh;

    private Float giaBan;

    private Float giaBanSauGiam;

    private Integer idSanPhamChiTiet;
    private String tenAnh;

    public PublicSanPhamResponse(Integer id, String tenSanPham, Float giaBan, String tenAnh) {
        this.id = id;
        this.tenSanPham = tenSanPham;
        this.giaBan = giaBan;
        this.tenAnh = tenAnh;
    }


}
