package com.example.datnsd26.repository;

import com.example.datnsd26.models.GioHang;
import com.example.datnsd26.models.KhachHang;
import com.example.datnsd26.models.TaiKhoan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GioHangRepository extends JpaRepository<GioHang, Integer> {

    Optional<GioHang> findByTaiKhoan(TaiKhoan taiKhoan);  // Tìm giỏ hàng theo tài khoản

    Optional<GioHang> findByIdKhachVangLai(String idKhachVangLai);// Tìm giỏ hàng theo guestId (khách vãng lai)
    
}
