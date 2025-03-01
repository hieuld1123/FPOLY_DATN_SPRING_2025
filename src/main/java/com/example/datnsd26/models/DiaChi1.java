package com.example.datnsd26.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "dia_chi")
public class DiaChi1 {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "dia_chi_cu_the", columnDefinition = "NVARCHAR(255)")
    private String diaChiCuThe;

    @Column(name = "tinh_thanh_pho", columnDefinition = "NVARCHAR(255)")
    private String tinhThanhPho;

    @Column(name = "quan_huyen", columnDefinition = "NVARCHAR(255)")
    private String quanHuyen;

    @Column(name = "xa_phuong", columnDefinition = "NVARCHAR(255)")
    private String xaPhuong;

}
