package com.example.datnsd26.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "NhanVien")
@Entity
public class NhanVien {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "NhanVienID")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "TaiKhoanID")
    private TaiKhoan idTaiKhoan;

    @Column(name = "MaNhanVien")
    private String maNhanvien;

    @Column(name = "HoTen")
    private String tenNhanVien;


    @Column(name = "DiaChi")
    private String diaChi;

    @Column(name = "GioiTinh")
    private Boolean gioiTinh;

    @Column(name = "HinhAnh")
    private String hinhAnh;

    @Column(name = "NgaySinh")
    private Date ngaySinh;

    @Column(name = "TrangThai")
    private Boolean trangThai;

    @Column(name = "NgayTao")
    private Date ngayTao;

    @Column(name = "NgayCapNhat")
    private Date ngayCapNhat;



}
