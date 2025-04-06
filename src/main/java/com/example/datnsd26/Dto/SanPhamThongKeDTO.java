package com.example.datnsd26.Dto;

import lombok.*;

import java.util.List;

@Data
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SanPhamThongKeDTO {
    private String maSanPham;
    private String tenSanPham;
    private Long tongSoLuongBan;
    private Double  tongDoanhThu;
}
