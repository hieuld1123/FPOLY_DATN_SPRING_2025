package com.example.datnsd26.Dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DiaChiDTO {
    private Integer id;
    private String diaChiCuThe;
    private String tinh;
    private String huyen;
    private String xa;
    private Timestamp ngayTao;
    private Timestamp ngayCapNhat;
    private Boolean trangThai;
    private Integer idKhachHang;
}
