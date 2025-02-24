package com.example.datnsd26.models;

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
@Table(name = "NhanVien")
@Entity
public class NhanVien {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "NhanVienID")
    private Integer id;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "TaiKhoanID")
    private TaiKhoan TaiKhoan;

    @Column(name = "MaNhanVien")
    private String maNhanvien;

    @Column(name = "HoTen")
    private String tenNhanVien;

    @Column(name = "DiaChi")
    private String diaChiCuThe;

    @Column(name = "Phuong")
    private String phuong;

    @Column(name = "Quan")
    private String quan;

    @Column(name = "Tinh")
    private String tinh;

    @Column(name = "Cccd")
    private String cccd;

    @Column(name = "GioiTinh")
    private Boolean gioiTinh;

    @Column(name = "HinhAnh")
    private String hinhAnh;

    @Column(name = "NgaySinh")
    @Temporal(TemporalType.DATE)
    private Date ngaySinh;

    @Column(name = "TrangThai")
    private Boolean trangThai;

    @Column(name = "NgayTao")
    private Timestamp ngayTao;

    @Column(name = "NgayCapNhat")
    private Timestamp ngayCapNhat;



}
