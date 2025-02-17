package com.example.datnsd26.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "DiaChi")
@Entity
public class DiaChi {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "DiaChiID")
    private Integer id;

    @Column(name = "DiaChiCuThe")
    @NotBlank(message = "Không được để trống")
    private String diaChiCuThe;

    @Column(name = "TinhThanhPho")
    @NotBlank(message = "Không được để trống")
    private String tinh;

    @Column(name = "QuanHuyen")
    @NotBlank(message = "Không được để trống")
    private String huyen;

    @Column(name = "XaPhuong")
    @NotBlank(message = "Không được để trống")
    private String xa;

    @Column(name = "QuocGia")
    @NotBlank(message = "Không được để trống")
    private String quocGia;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    @Column(nullable = false, updatable = false)
    private LocalDateTime ngayTao;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    @Column(nullable = false)
    private LocalDateTime ngayCapNhat;

    @PrePersist
    public void onPrePersist() {
        this.ngayTao = LocalDateTime.now();  // Đặt ngày tạo là ngày hiện tại
        this.ngayCapNhat = LocalDateTime.now();  // Đặt ngày cập nhật là ngày hiện tại
    }

    @PreUpdate
    public void onPreUpdate() {
        this.ngayCapNhat = LocalDateTime.now();  // Cập nhật lại ngày cập nhật mỗi khi thay đổi
    }
}

