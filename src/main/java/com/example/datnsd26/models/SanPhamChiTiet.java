package com.example.datnsd26.models;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id"
)

@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "sanphamchitiet")
public class SanPhamChiTiet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    String qrcode;

    String masanphamchitiet;

    String mota;

    Boolean gioitinh;

    Integer soluong;

    BigDecimal giatien;

    @Column(name = "gia_ban_sau_giam")
    private Float giaBanSauGiam;

    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime ngaytao;

    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime ngaycapnhat;

    @Column(columnDefinition = "BIT DEFAULT 1")
    Boolean trangthai;


    @ManyToOne
    @JoinColumn(name = "idsanpham")
    SanPham sanpham;

    @ManyToOne
    @JoinColumn(name = "idkichco")
    KichCo kichco;

    @ManyToOne
    @JoinColumn(name = "idmausac")
    MauSac mausac;

    @ManyToOne
    @JoinColumn(name = "idchatlieu")
    ChatLieu chatlieu;

    @ManyToOne
    @JoinColumn(name = "idthuonghieu")
    ThuongHieu thuonghieu;

    @ManyToOne
    @JoinColumn(name = "iddegiay")
    DeGiay degiay;

    @OneToMany(mappedBy = "sanphamchitiet", fetch = FetchType.EAGER)
    List<Anh> anh;
    @OneToMany(mappedBy = "sanphamchitiet", fetch = FetchType.EAGER)


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SanPhamChiTiet that = (SanPhamChiTiet) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
