package com.example.datnsd26.repository;

import com.example.datnsd26.models.KhuyenMai;
import com.example.datnsd26.models.KhuyenMaiChitiet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface KhuyenMaiChiTietRepository extends JpaRepository<KhuyenMaiChitiet, Long> {

    @Transactional
    void deleteByKhuyenMai_Id(Long id);

    @Query("SELECT kmct FROM KhuyenMaiChitiet kmct " +
            "WHERE kmct.sanPhamChiTiet.id = :sanPhamId " +
            "AND kmct.khuyenMai.trangThai = 1 " +
            "AND :now BETWEEN kmct.khuyenMai.thoiGianBatDau AND kmct.khuyenMai.thoiGianKetThuc")
    KhuyenMaiChitiet findActivePromotionBySanPham(Long sanPhamId, LocalDateTime now);

    @Query("SELECT kmct FROM KhuyenMaiChitiet kmct JOIN FETCH kmct.sanPhamChiTiet WHERE kmct.khuyenMai.id = :khuyenMaiId")
    List<KhuyenMaiChitiet> findByKhuyenMai_Id(Long khuyenMaiId);

    @Query("SELECT kmct FROM KhuyenMaiChitiet kmct " +
            "WHERE kmct.sanPhamChiTiet.id = :sanPhamId " +
            "AND kmct.khuyenMai.trangThai = 1 " +
            "AND kmct.khuyenMai.thoiGianBatDau <= :now " +
            "AND kmct.khuyenMai.thoiGianKetThuc >= :now")
    List<KhuyenMaiChitiet> findAllActivePromotionsBySanPham(Long sanPhamId, LocalDateTime now);

    @Query("SELECT kmct FROM KhuyenMaiChitiet kmct " +
            "WHERE kmct.khuyenMai.id = :khuyenMaiId " +
            "ORDER BY kmct.sanPhamChiTiet.id")
    List<KhuyenMaiChitiet> findByKhuyenMai_IdOrderBySanPhamChiTietAsc(Long khuyenMaiId);

    @Query("SELECT kmct.sanPhamChiTiet.id FROM KhuyenMaiChitiet kmct " +
            "JOIN kmct.khuyenMai km " +
            "WHERE km.trangThai = 1")
    List<Long> findSanPhamDangKhuyenMai();


}
