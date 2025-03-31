package com.example.datnsd26.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "khuyen_mai_chi_tiet")
public class KhuyenMaiChitiet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_khuyen_mai", nullable = false)
    private KhuyenMai khuyenMai;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_san_pham_chi_tiet", nullable = false)
    private SanPhamChiTiet sanPhamChiTiet;

    @Column(name = "so_tien_giam", nullable = false)
    private Float soTienGiam;

    @Column(name = "trang_thai")
    private Integer trangThai = 1;

    @PrePersist
    protected void onCreate() {
        if (this.trangThai == null) {
            this.trangThai = 1;
        }
    }

    public KhuyenMaiChitiet(KhuyenMai khuyenMai, SanPhamChiTiet sanPhamChiTiet, Float soTienGiam, int trangThai) {
        this.khuyenMai = khuyenMai;
        this.sanPhamChiTiet = sanPhamChiTiet;
        this.soTienGiam = soTienGiam;
        this.trangThai = trangThai;
    }
}
