package com.example.datnsd26.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "voucher")
public class Voucher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false,name = "ma_voucher", unique = true , columnDefinition = "NVARCHAR(255)")
    private String maVoucher;

    @Column(nullable = false , name = "ten_voucher", columnDefinition = "NVARCHAR(255)")
    private String tenVoucher;

    @Column(nullable = false ,name = "hinh_thuc_giam",columnDefinition = "NVARCHAR(255)")
    private String hinhThucGiam;

    @Column(nullable = false, name = "so_luong")
    private Integer soLuong;

    @Column(nullable = false ,name = "gia_tri_giam")
    private Double giaTriGiam;

    @Column(nullable = false ,name = "gia_tri_giam_toi_thieu")
    private Double giaTriGiamToiThieu;

    @Column(nullable = true,name = "gia_tri_giam_toi_da")
    private Double giaTriGiamToiDa;


    @Column(nullable = false,name = "ngay_bat_dau")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime ngayBatDau;

    public void setNgayBatDau(LocalDateTime ngayBatDau) {
        if (ngayBatDau != null) {
            this.ngayBatDau = ngayBatDau.withSecond(0).withNano(0);
        }
    }

    @Column(nullable = false ,name = "ngay_ket_thuc")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime ngayKetThuc;

    public void setNgayKetThuc(LocalDateTime ngayKetThuc) {  // Sửa tên tham số
        if (ngayKetThuc != null) {
            this.ngayKetThuc = ngayKetThuc.withSecond(0).withNano(0);
        }
    }


    @Column(nullable = false, updatable = false ,name = "ngay_tao")
    private LocalDateTime ngayTao;

    @Column(nullable = false ,name = "ngay_cap_nhat")
    private LocalDateTime ngayCapNhat;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);

        if (this.ngayBatDau != null) {
            this.ngayBatDau = this.ngayBatDau.withSecond(0).withNano(0);
        }

        if (this.ngayKetThuc != null) {
            this.ngayKetThuc = this.ngayKetThuc.withSecond(0).withNano(0);
        }

        this.ngayTao = now;
        this.ngayCapNhat = now;
        this.trangThai = (this.trangThai == null) ? determineVoucherStatus() : this.trangThai;
    }


    @Column(nullable = false, name = "trang_thai")
    private Integer trangThai = 0;

    @PreUpdate
    protected void onUpdate() {
        this.ngayCapNhat = LocalDateTime.now().withSecond(0).withNano(0);
        this.trangThai = determineVoucherStatus();
    }

    private Integer determineVoucherStatus() {
        LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);

        if (now.isBefore(ngayBatDau)) {
            return 0; // Chưa bắt đầu
        } else if (now.isAfter(ngayKetThuc)) {
            return 2; // Hết hạn
        } else {
            return 1; // Đang hoạt động
        }
    }

}