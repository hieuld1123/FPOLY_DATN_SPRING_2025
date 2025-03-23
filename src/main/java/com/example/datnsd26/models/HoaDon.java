package com.example.datnsd26.models;

import jakarta.persistence.*;
import lombok.*;
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
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HoaDon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_khach_hang", referencedColumnName = "id", nullable = true)
    private KhachHang khachHang; // Có thể null

    @ManyToOne
    @JoinColumn(name = "id_nhan_vien", referencedColumnName = "id", nullable = true)
    private NhanVien nhanVien; // Có thể null

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

//    @NotBlank(message = "Họ và tên không được để trống")
    @Column(name = "ten_nguoi_nhan", columnDefinition = "nvarchar(255)")
    private String tenNguoiNhan;

//    @NotBlank(message = "Số điện thoại không được để trống")
//    @Pattern(regexp = "^(0[2-9]|84[2-9])[0-9]{8}$", message = "Số điện thoại không hợp lệ")
    @Column(name = "sdt_nguoi_nhan", columnDefinition = "nvarchar(255)")
    private String sdtNguoiNhan;

//    @NotBlank(message = "Email không được để trống")
//    @Email(message = "Email không hợp lệ")
    @Column(name = "email", columnDefinition = "NVARCHAR(255)")
    private String email;

//    @NotBlank(message = "Vui lòng chọn tỉnh/thành phố")
    @Column(name = "tinh", columnDefinition = "NVARCHAR(100)")
    private String tinh;

//    @NotBlank(message = "Vui lòng chọn quận/huyện")
    @Column(name = "quan", columnDefinition = "NVARCHAR(100)")
    private String quan;

//    @NotBlank(message = "Vui lòng chọn xã/phường")
    @Column(name = "xa", columnDefinition = "NVARCHAR(100)")
    private String xa;

//    @NotBlank(message = "Địa chỉ cụ thể không được để trống")
//    @Size(min = 5, message = "Địa chỉ cụ thể phải có ít nhất 5 ký tự")
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

    @Column(name = "thanh_toan")
    private boolean thanhToan;

    @OneToMany(mappedBy = "hoaDon", cascade = CascadeType.ALL)
    private List<HoaDonChiTiet> danhSachSanPham;
}
