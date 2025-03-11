package com.example.datnsd26.repository;

import com.example.datnsd26.models.HoaDon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HoaDonRepository extends JpaRepository<HoaDon, Integer> {

    @Query("SELECT MAX(h.maHoaDon) FROM HoaDon h")
    String findLastMaHoaDon();

    @Query("FROM HoaDon hd WHERE hd.trangThai = :status")
    List<HoaDon> findAllInvoiceByStatus(@Param("status") String status);
}
