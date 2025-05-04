package com.example.datnsd26.models;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.text.DecimalFormat;
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

    @Column(nullable = false, name = "ma_voucher", unique = true, columnDefinition = "NVARCHAR(255)")
    private String maVoucher;

    @Column(nullable = false, name = "ten_voucher", columnDefinition = "NVARCHAR(255)")
    private String tenVoucher;

    @Column(nullable = false, name = "hinh_thuc_giam", columnDefinition = "NVARCHAR(255)")
    private String hinhThucGiam;

    @Column(nullable = false, name = "so_luong")
    private Integer soLuong;

    @Column(nullable = false, name = "gia_tri_giam")
    private Float giaTriGiam;

    @Column(nullable = false, name = "gia_tri_giam_toi_thieu")
    private Float giaTriGiamToiThieu;

    @Column(nullable = true, name = "gia_tri_giam_toi_da")
    private Float giaTriGiamToiDa;

    @Column(nullable = false, name = "cong_khai")
    private Boolean congKhai = true;

    @Column(nullable = false, name = "ngay_bat_dau")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime ngayBatDau;

    @Column(nullable = false, name = "ngay_ket_thuc")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime ngayKetThuc;

    @Column(nullable = false, updatable = false, name = "ngay_tao")
    private LocalDateTime ngayTao;

    @Column(nullable = false, name = "ngay_cap_nhat")
    private LocalDateTime ngayCapNhat;

    @Column(nullable = false, name = "trang_thai")
    private Integer trangThai = 0;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);
        this.ngayTao = now;
        this.ngayCapNhat = now;

        // Xử lý mặc định nếu cần
        if (this.trangThai == null) {
            this.trangThai = 0;
        }

        // Đảm bảo ngày tháng không có giây và nano giây
        if (this.ngayBatDau != null) {
            this.ngayBatDau = this.ngayBatDau.withSecond(0).withNano(0);
        }
        if (this.ngayKetThuc != null) {
            this.ngayKetThuc = this.ngayKetThuc.withSecond(0).withNano(0);
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.ngayCapNhat = LocalDateTime.now().withSecond(0).withNano(0);
    }



}