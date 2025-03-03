package com.example.datnsd26.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "dia_chi")
@Entity
public class DiaChi {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "dia_chi_cu_the")
    private String diaChiCuThe;

    @Column(name = "tinh_thanh_pho")
    private String tinh;

    @Column(name = "quan_huyen")
    private String huyen;

    @Column(name = "xa_phường")
    private String xa;

    @Column(name = "ngay_tao",nullable = true)
    private Timestamp ngayTao;

    @Column(name = "ngay_cap_nhat",nullable = true)
    private Timestamp ngayCapNhat;

    @Column(name = "trang_thai")
    private Boolean trangThai;

    @ManyToOne
    @JoinColumn(name = "id_khach_hang", nullable = false)
    private KhachHang khachHang;

    @Column(name = "is_default")
    private Boolean isDefault; // Đánh dấu địa chỉ mặc định


}

