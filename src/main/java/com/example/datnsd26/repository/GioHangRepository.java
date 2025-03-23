package com.example.datnsd26.repository;

import com.example.datnsd26.models.GioHang;
import com.example.datnsd26.models.KhachHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GioHangRepository extends JpaRepository<GioHang, Integer> {
    Optional<GioHang> findByKhachHang(KhachHang khachHang);
}
