package com.example.datnsd26.Dto;

import com.example.datnsd26.models.NhanVien;
import com.example.datnsd26.models.TaiKhoan;
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

    private TaiKhoan taiKhoan;

    private String maNhanVien;

    private String tenNhanVien;

    private String diaChiCuThe;

    private String phuong;

    private String quan;

    private String tinh;

    private String cccd;

    private Boolean gioiTinh;

    private String hinhAnh;

    private Date ngaySinh;
    private String email;

    private String sdt;

    private TaiKhoan.Role vaiTro;

    private Boolean trangThai;

    private Timestamp ngayTao;

    private Timestamp ngayCapNhat;

}
