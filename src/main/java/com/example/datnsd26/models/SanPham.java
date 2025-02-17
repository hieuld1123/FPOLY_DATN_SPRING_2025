package com.example.datnsd26.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Table(name = "san_pham")
public class SanPham {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_danh_muc", referencedColumnName = "id")
    private DanhMuc danhMuc;

    @ManyToOne
    @JoinColumn(name = "id_chat_lieu", referencedColumnName = "id")
    private ChatLieu chatLieu;

    @ManyToOne
    @JoinColumn(name = "id_thuong_hieu", referencedColumnName = "id")
    private ThuongHieu thuongHieu;

    @Column(name = "ma_san_pham", columnDefinition = "NVARCHAR(255)")
    private String maSanPham;

    @Column(name = "ten_san_pham", columnDefinition = "NVARCHAR(255)")
    private String tenSanPham;

    @Column(name = "hinh_anh", columnDefinition = "NVARCHAR(255)")
    private String hinhAnh;

    @Column(name = "mo_ta", columnDefinition = "NVARCHAR(255)")
    private String moTa;

    @Column(name = "ngay_tao")
    @Temporal(TemporalType.TIMESTAMP)
    private Date ngayTao;

    @Column(name = "ngay_cap_nhat")
    @Temporal(TemporalType.TIMESTAMP)
    private Date ngayCapNhat;

    @Column(name = "trang_thai")
    private Boolean trangThai;

}
