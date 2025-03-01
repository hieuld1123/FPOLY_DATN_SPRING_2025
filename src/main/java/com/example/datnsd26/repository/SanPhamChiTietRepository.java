package com.example.datnsd26.repository;

import com.example.datnsd26.models.GioHangChiTiet;
import com.example.datnsd26.models.SanPhamChiTiet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SanPhamChiTietRepository extends JpaRepository<SanPhamChiTiet, Integer> {

}
