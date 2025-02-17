package com.example.datnsd26.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "san_pham_chi_tiet")
public class SanPhamChiTiet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_san_pham", referencedColumnName = "id")
    private SanPham sanPham;

    @ManyToOne
    @JoinColumn(name = "id_mau_sac", referencedColumnName = "id")
    private MauSac mauSac;

    @ManyToOne
    @JoinColumn(name = "id_kich_thuoc", referencedColumnName = "id")
    private KichThuoc kichThuoc;

    @Column(name = "ma_san_pham_chi_tiet", columnDefinition = "NVARCHAR(255)")
    private String maSanPhamChiTiet;

    @Column(name = "ten_san_pham_chi_tiet", columnDefinition = "NVARCHAR(255)")
    private String tenSanPhamChiTiet;

    @Column(name = "so_luong")
    private Integer soLuong;

    @Column(name = "gia_ban")
    private Float giaBan;

    @Column(name = "gia_ban_sau_giam")
    private Float giaBanSauGiam;

    @Column(name = "ghi_chu", columnDefinition = "NVARCHAR(255)")
    private String ghiChu;

    @Column(name = "ngay_tao")
    @Temporal(TemporalType.TIMESTAMP)
    private Date ngayTao;

    @Column(name = "ngay_cap_nhat")
    @Temporal(TemporalType.TIMESTAMP)
    private Date ngayCapNhat;

    @Column(name = "trang_thai")
    private Boolean trangThai;

}
