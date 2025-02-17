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
@Table(name = "khach_hang")
public class KhachHang1 {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_tai_khoan", nullable = true)
    private TaiKhoan taiKhoan;

    @ManyToOne
    @JoinColumn(name = "id_dia_chi")
    private DiaChi1 diaChi;

    @Column(name = "ho_ten", columnDefinition = "NVARCHAR(255)")
    private String hoTen;

    @Column(name = "ma_khach_hang", columnDefinition = "NVARCHAR(255)")
    private String maKhachHang;

    @Column(name = "gioi_tinh")
    private Boolean gioiTinh;

    @Column(name = "hinh_anh", columnDefinition = "NVARCHAR(255)")
    private String hinhAnh;

    @Column(name = "ngay_sinh")
    private Date ngaySinh;

    @Column(name = "ngay_tao")
    @Temporal(TemporalType.TIMESTAMP)
    private Date ngayTao;

    @Column(name = "ngay_cap_nhat")
    @Temporal(TemporalType.TIMESTAMP)
    private Date ngayCapNhat;

    @Column(name = "trang_thai")
    private Boolean trangThai;

}
