package com.example.datnsd26.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tai_khoan")

public class TaiKhoan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String email;

    private String sdt;
    private String matKhau;

    @Enumerated(EnumType.STRING)
    private Role vaiTro;

    private Boolean trangThai;

    public enum Role {
        ADMIN, EMPLOYEE, CUSTOMER
    }
}
