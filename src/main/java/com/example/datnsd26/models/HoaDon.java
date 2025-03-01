package com.example.datnsd26.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Entity
@Table(name = "hoa_don")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HoaDon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_khach_hang", referencedColumnName = "id", nullable = true)
    private KhachHang1 khachHang1; // Có thể null

    @ManyToOne
    @JoinColumn(name = "id_nhan_vien", referencedColumnName = "id", nullable = true)
    private NhanVien1 nhanVien; // Có thể null

    @Column(name = "ma_hoa_don", columnDefinition = "nvarchar(255)")
    private String maHoaDon;

    @Column(name = "ngay_tao")
    @Temporal(TemporalType.TIMESTAMP)
    private Date ngayTao;

    @Column(name = "ngay_cap_nhat")
    @Temporal(TemporalType.TIMESTAMP)
    private Date ngayCapNhat;

    @Column(name = "hinh_thuc_mua_hang", columnDefinition = "nvarchar(255)")
    private String hinhThucMuaHang;

    @Column(name = "ten_nguoi_nhan", columnDefinition = "nvarchar(255)")
    private String tenNguoiNhan;

    @Column(name = "sdt_nguoi_nhan", columnDefinition = "nvarchar(255)")
    private String sdtNguoiNhan;

    @Column(name = "dia_chi_nguoi_nhan", columnDefinition = "nvarchar(255)")
    private String diaChiNguoiNhan;

    @Column(name = "phi_van_chuyen")
    private Float phiVanChuyen;

    @Column(name = "tong_tien")
    private Float tongTien;

    @Column(name = "phuong_thuc_thanh_toan", columnDefinition = "nvarchar(255)")
    private String phuongThucThanhToan;

    @Column(name = "ghi_chu", columnDefinition = "nvarchar(255)")
    private String ghiChu;

    @Column(name = "trang_thai", columnDefinition = "nvarchar(255)")
    private String trangThai;

    @OneToMany(mappedBy = "hoaDon", cascade = CascadeType.ALL)
    private List<HoaDonChiTiet> danhSachSanPham;
}
