package com.example.datnsd26.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.sql.Date;
import java.sql.Timestamp;



@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "nhan_vien")
@Entity
public class NhanVien {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "id_tai_khoan")
    private TaiKhoan idTaiKhoan;

    @Column(name = "ma_nhan_vien")
    private String maNhanvien;

    @Column(name = "ho_ten", columnDefinition = "NVARCHAR(255)")
    private String tenNhanVien;

    @Column(name = "dia_chi_cu_the", columnDefinition = "NVARCHAR(255)")
    private String diaChiCuThe;

    @Column(name = "tinh_thanh_pho", columnDefinition = "NVARCHAR(255)")
    private String tinh;

    @Column(name = "quan_huyen", columnDefinition = "NVARCHAR(255)")
    private String huyen;

    @Column(name = "xa_phường", columnDefinition = "NVARCHAR(255)")
    private String xa;

    @Column(name = "gioi_tinh")
    private Boolean gioiTinh;

    @Column(name = "hinh_anh")
    private String hinhAnh;

    @Column(name = "ngay_sinh")
    @Temporal(TemporalType.DATE)
    private Date ngaySinh;

    @Column(name = "trang_thai")
    private Boolean trangThai;

    @Column(name = "ngay_tao")
    private Timestamp ngayTao;

    @Column(name = "ngay_cap_nhat")
    private Timestamp ngayCapNhat;



}
