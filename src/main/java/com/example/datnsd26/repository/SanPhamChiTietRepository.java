package com.example.datnsd26.repository;

import com.example.datnsd26.models.SanPhamChiTiet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SanPhamChiTietRepository extends JpaRepository<SanPhamChiTiet, Integer> {

    @Query("FROM SanPhamChiTiet sp WHERE sp.sanPham.tenSanPham like %:keyword% AND sp.soLuong >= 1")
    List<SanPhamChiTiet> findByName(String keyword);
}
