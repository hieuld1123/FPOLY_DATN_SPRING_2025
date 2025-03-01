package com.example.datnsd26.Dto;

import com.example.datnsd26.models.DiaChi;
import com.example.datnsd26.models.TaiKhoan;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class KhachHangDto {
    private Integer id;
    private TaiKhoan taiKhoan;
    private String tenKhachHang;
    private String maKhachHang;
    private Boolean gioiTinh;
    private String hinhAnh;
    private Date ngaySinh;
    private Boolean trangThai;
    private List<DiaChiDTO> listDiaChi;
    private String diaChiCuThe;
    private String tinh;
    private String huyen;
    private String xa;

    private String email;
    private String sdt;
    private String matKhau;
    private TaiKhoan.Role vaiTro;
    private Timestamp ngayTao;
    private Timestamp ngayCapNhat;

}
