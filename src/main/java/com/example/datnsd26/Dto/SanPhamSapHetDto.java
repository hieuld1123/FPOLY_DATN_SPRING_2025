package com.example.datnsd26.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SanPhamSapHetDto {
    private String maSanPham;
    private String tenSanPham;
    private String kichThuoc;
    private Integer soLuongTon;
}
