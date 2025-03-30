package com.example.datnsd26.repository;

import com.example.datnsd26.models.KhuyenMai;
import com.example.datnsd26.models.SanPhamChiTiet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface KhuyenMaiRepository extends JpaRepository<KhuyenMai, Long> {


    @Query("SELECT km FROM KhuyenMai km " +
            "WHERE (:tenChienDich IS NULL OR km.tenChienDich LIKE %:tenChienDich%) " +
            "AND (:trangThai IS NULL OR km.trangThai = :trangThai) " +
            "AND (:startDate IS NULL OR km.thoiGianBatDau >= :startDate) " +
            "AND (:endDate IS NULL OR km.thoiGianKetThuc <= :endDate)")
    Page<KhuyenMai> searchKhuyenMai(@Param("tenChienDich") String tenChienDich,
                                    @Param("trangThai") Integer trangThai,
                                    @Param("startDate") LocalDateTime startDate,
                                    @Param("endDate") LocalDateTime endDate,
                                    Pageable pageable);
}



