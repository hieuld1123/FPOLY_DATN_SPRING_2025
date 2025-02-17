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
@Table(name = "KhachHang")
@Entity
public class KhachHang {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "KhachHangID")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "TaiKhoanID")
    private TaiKhoan idTaiKhoan;

    @ManyToOne
    @JoinColumn(name = "DiaChiID")
    private DiaChi idDiaChi;

    @Column(name = "HoTen")
    private String tenKhachHang;

    @Column(name = "MaKhachHang")
    private String maKhachHang;

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
