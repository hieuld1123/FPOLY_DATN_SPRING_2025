package com.example.datnsd26.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

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

    @OneToOne
    @JoinColumn(name = "TaiKhoanID")
    private TaiKhoan idTaiKhoan;


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

    @Column(name = "NgayTao",nullable = false, updatable = false)
    private LocalDateTime ngayTao;


    @Column(name = "NgayCapNhat",nullable = false)
    private LocalDateTime ngayCapNhat;

    @PrePersist
    public void onPrePersist() {
        this.ngayTao = LocalDateTime.now();  // Đặt ngày tạo là ngày hiện tại
        this.ngayCapNhat = LocalDateTime.now();  // Đặt ngày cập nhật là ngày hiện tại
    }

    @PreUpdate
    public void onPreUpdate() {
        this.ngayCapNhat = LocalDateTime.now();  // Cập nhật lại ngày cập nhật mỗi khi thay đổi
    }

}
