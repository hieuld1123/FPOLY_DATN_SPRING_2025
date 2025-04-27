package com.example.datnsd26.controller.response;

import lombok.*;

import java.util.List;

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

    private String thuongHieu;
    private List<String> mauSac;

}
