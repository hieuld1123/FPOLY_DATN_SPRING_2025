package com.example.datnsd26.models;

import jakarta.persistence.*;
import lombok.*;

import java.text.DecimalFormat;
import java.time.LocalDateTime;

@ToString(exclude = {"gioHang"}) // Loại trừ trường gioHang để tránh đệ quy
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Entity
@Table(name = "gio_hang_chi_tiet")
public class GioHangChiTiet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_gio_hang", referencedColumnName = "id")
    private GioHang gioHang;

    @ManyToOne
    @JoinColumn(name = "id_san_pham_chi_tiet", referencedColumnName = "id")
    private SanPhamChiTiet sanPhamChiTiet;

    @Column(name = "so_luong")
    private int soLuong;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;

    @Column(name = "ngay_sua")
    private LocalDateTime ngaySua;

    @PrePersist
    protected void onCreate() {
        ngayTao = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        ngaySua = LocalDateTime.now();
    }

    public float getTongTien() {
        return soLuong * sanPhamChiTiet.getGiaBanSauGiam();
    }

    public String getTongTienFormatted() {
        DecimalFormat formatter = new DecimalFormat("#,###");
        return formatter.format(getTongTien()) + " VND";
    }

}
