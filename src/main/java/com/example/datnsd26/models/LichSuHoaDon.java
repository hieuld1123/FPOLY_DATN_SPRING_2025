package com.example.datnsd26.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "lich_su_hoa_don")
public class LichSuHoaDon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "trang_thai", columnDefinition = "NVARCHAR(50)")
    private String trangThai;

    @Column(name = "thoi_gian")
    @Temporal(TemporalType.TIMESTAMP)
    @CreationTimestamp
    private Date thoiGian;

    @ManyToOne
    @JoinColumn(name = "id_hoa_don")
    private HoaDon hoaDon;
}
