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
            "AND kmct.khuyenMai.thoiGianBatDau < :endDate " +
            "AND kmct.khuyenMai.thoiGianKetThuc > :startDate " +
            "AND kmct.khuyenMai.trangThai IN (0, 1)")
    List<KhuyenMaiChitiet> findOverlappingPromotions(Long sanPhamId,
                                                     LocalDateTime startDate,
                                                     LocalDateTime endDate);

    List<KhuyenMaiChitiet> findBySanPhamChiTietId(Long sanPhamId);

    @Query("SELECT kmct FROM KhuyenMaiChitiet kmct " +
            "WHERE kmct.khuyenMai.trangThai = 1")
    List<KhuyenMaiChitiet> findActivePromotions();

    @Query("SELECT kmct FROM KhuyenMaiChitiet kmct " +
            "WHERE kmct.sanPhamChiTiet.id = :sanPhamId " +
            "AND kmct.khuyenMai.trangThai = 1 " +
            "AND :now BETWEEN kmct.khuyenMai.thoiGianBatDau AND kmct.khuyenMai.thoiGianKetThuc")
    KhuyenMaiChitiet findActivePromotionBySanPham(Long sanPhamId, LocalDateTime now);

    @Transactional
    void deleteBySanPhamChiTiet_Id(Long sanPhamId);

    boolean existsBySanPhamChiTietIdAndTrangThai(Long sanPhamChiTietId, int trangThai);

    @Query("SELECT kmct FROM KhuyenMaiChitiet kmct " +
            "WHERE kmct.khuyenMai.id = :khuyenMaiId")
    List<KhuyenMaiChitiet> findByKhuyenMaiId(Long khuyenMaiId);

    @Query("SELECT kmct FROM KhuyenMaiChitiet kmct " +
            "WHERE kmct.sanPhamChiTiet.id = :sanPhamId " +
            "AND kmct.khuyenMai.trangThai = 1 " +
            "ORDER BY kmct.khuyenMai.thoiGianBatDau DESC")
    List<KhuyenMaiChitiet> findActiveBySanPhamId(Long sanPhamId);

    @Query("SELECT COUNT(kmct) > 0 FROM KhuyenMaiChitiet kmct " +
            "WHERE kmct.sanPhamChiTiet.id = :sanPhamId " +
            "AND kmct.khuyenMai.trangThai = 1")
    boolean existsActiveBySanPhamId(Long sanPhamId);

    @Query("SELECT kmct FROM KhuyenMaiChitiet kmct " +
            "WHERE kmct.khuyenMai.trangThai = 1 " +
            "AND CURRENT_TIMESTAMP BETWEEN kmct.khuyenMai.thoiGianBatDau AND kmct.khuyenMai.thoiGianKetThuc")
    List<KhuyenMaiChitiet> findCurrentActivePromotions();



    @Query("SELECT kmct FROM KhuyenMaiChitiet kmct JOIN FETCH kmct.sanPhamChiTiet WHERE kmct.khuyenMai.id = :khuyenMaiId")
    List<KhuyenMaiChitiet> findByKhuyenMai_Id(Long khuyenMaiId);

    @Transactional
    void deleteByKhuyenMai(KhuyenMai khuyenMai);

    @Query("SELECT DISTINCT kmct.sanPhamChiTiet.id FROM KhuyenMaiChitiet kmct WHERE kmct.khuyenMai.id = :khuyenMaiId")
    List<Long> findSelectedProductIds(Long khuyenMaiId);

    @Query("SELECT kmct.soTienGiam FROM KhuyenMaiChitiet kmct " +
            "WHERE kmct.khuyenMai.id = :khuyenMaiId AND kmct.sanPhamChiTiet.id = :sanPhamId")
    Float findGiaTriGiamBySanPhamAndKhuyenMai(Long khuyenMaiId, Long sanPhamId);

    List<KhuyenMaiChitiet> findByKhuyenMai_IdAndTrangThaiGreaterThanEqual(Long khuyenMaiId, Integer trangThai);

    Optional<KhuyenMaiChitiet> findByKhuyenMai_IdAndSanPhamChiTiet_Id(Long khuyenMaiId, Integer sanPhamId);

    @Query("SELECT spct.id, kmct.soTienGiam " +
            "FROM KhuyenMaiChitiet kmct " +
            "JOIN kmct.sanPhamChiTiet spct " +
            "WHERE kmct.khuyenMai.id = :khuyenMaiId")
    List<Object[]> findSanPhamGiamGiaByKhuyenMaiId(@Param("khuyenMaiId") Long khuyenMaiId);


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
}
