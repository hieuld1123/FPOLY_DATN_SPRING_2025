package com.example.datnsd26.Dto;

import com.example.datnsd26.models.NhanVien;
import com.example.datnsd26.models.TaiKhoan;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Date;
import java.sql.Timestamp;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NhanVienTKDto {
    private Integer id;
    private TaiKhoan idTaiKhoan;
    private String maNhanvien;
    private String tenNhanVien;
    private String diaChiCuThe;
    private String tinh;
    private String huyen;
    private String xa;
    private Boolean gioiTinh;
    private String hinhAnh;
    private Date ngaySinh;
    private Boolean trangThai;
    private Timestamp ngayTao;
    private Timestamp ngayCapNhat;
    private String email;
    private String sdt;
    private String matKhau;
    private TaiKhoan.Role vaiTro;
    private Boolean trangThaiTK;
    private String resetToken;

}
