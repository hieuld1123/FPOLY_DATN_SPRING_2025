package com.example.datnsd26.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "hoa_don_chi_tiet")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HoaDonChiTiet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_hoa_don", referencedColumnName = "id", nullable = false)
    private HoaDon hoaDon;

    @ManyToOne
    @JoinColumn(name = "id_san_pham_chi_tiet", referencedColumnName = "id", nullable = false)
    private SanPhamChiTiet sanPhamChiTiet;

    @Column(name = "so_luong")
    private Integer soLuong;

    @Column(name = "gia_tien_sau_giam")
    private Float giaTienSauGiam;
}
