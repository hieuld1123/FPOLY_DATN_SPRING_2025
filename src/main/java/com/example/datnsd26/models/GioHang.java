package com.example.datnsd26.models;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "gio_hang")
public class GioHang {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_tai_khoan", referencedColumnName = "id", nullable = true)
    private TaiKhoan taiKhoan; // Có thể null nếu là khách vãng lai

    @Column(name = "id_khach_vang_lai", unique = true, length = 36)
    private String idKhachVangLai; // UUID nhận diện khách vãng lai

    @OneToMany(mappedBy = "gioHang", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<GioHangChiTiet> chiTietList = new ArrayList<>();

    @Column(name = "ngay_tao")
    @Temporal(TemporalType.TIMESTAMP)
    private Date ngayTao = new Date();
}
