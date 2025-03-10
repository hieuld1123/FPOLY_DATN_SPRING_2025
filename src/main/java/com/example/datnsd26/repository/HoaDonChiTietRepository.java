package com.example.datnsd26.repository;

import com.example.datnsd26.models.HoaDonChiTiet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HoaDonChiTietRepository extends JpaRepository<HoaDonChiTiet, Integer> {

    @Query("FROM HoaDonChiTiet ct WHERE ct.hoaDon.id = :invoiceId and ct.sanPhamChiTiet.id = :productId")
    Optional<HoaDonChiTiet> findHoaDonChiTietByIdAndProductId(int invoiceId, int productId);
}
