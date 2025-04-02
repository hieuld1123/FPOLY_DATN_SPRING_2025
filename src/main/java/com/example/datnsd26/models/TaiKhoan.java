package com.example.datnsd26.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tai_khoan")
@Builder
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
        ADMIN, EMPLOYEE, CUSTOMER;
        public String getDisplayName() {
            switch (this) {
                case ADMIN:
                    return "Admin";
                case EMPLOYEE:
                    return "Nhân viên";
                default:
                    return "Khách hàng";
            }
        }
    }
    @Column(name = "reset_token")
    private String resetToken;

}
