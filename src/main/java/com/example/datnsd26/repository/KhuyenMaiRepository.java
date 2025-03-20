package com.example.datnsd26.repository;

import com.example.datnsd26.models.KhuyenMai;
import com.example.datnsd26.models.SanPhamChiTiet;
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
    @Query("SELECT km FROM KhuyenMai km WHERE " +
            "(:tenChienDich IS NULL OR km.tenChienDich LIKE CONCAT('%', :tenChienDich, '%')) AND " +
            "(:trangThai IS NULL OR km.trangThai = :trangThai) AND " +
            "(:ngayBatDau IS NULL OR km.thoiGianBatDau >= :ngayBatDau) AND " +
            "(:ngayKetThuc IS NULL OR km.thoiGianKetThuc < :ngayKetThuc)")
    List<KhuyenMai> findByFilters(
            @Param("tenChienDich") String tenChienDich,
            @Param("trangThai") Integer trangThai,
            @Param("ngayBatDau") LocalDateTime ngayBatDau,
            @Param("ngayKetThuc") LocalDateTime ngayKetThuc);

    // Tìm khuyến mãi đang hoạt động
    @Query("SELECT km FROM KhuyenMai km WHERE km.trangThai = 1 AND " +
            "CURRENT_TIMESTAMP BETWEEN km.thoiGianBatDau AND km.thoiGianKetThuc")
    List<KhuyenMai> findKhuyenMaiDangHoatDong();

    // Tìm khuyến mãi đã hết hạn
    @Query("SELECT km FROM KhuyenMai km WHERE " +
            "km.trangThai = 1 AND km.thoiGianKetThuc < CURRENT_TIMESTAMP")
    List<KhuyenMai> findKhuyenMaiHetHan();

    // Kiểm tra có khuyến mãi đang hoạt động tại thời điểm cụ thể
    @Query("SELECT CASE WHEN COUNT(km) > 0 THEN true ELSE false END FROM KhuyenMai km " +
            "WHERE km.trangThai = 1 AND :thoiDiem BETWEEN km.thoiGianBatDau AND km.thoiGianKetThuc")
    boolean kiemTraKhuyenMaiDangHoatDong(@Param("thoiDiem") LocalDateTime thoiDiem);

    // Cập nhật trạng thái khuyến mãi hết hạn
    @Modifying
    @Query("UPDATE KhuyenMai km SET km.trangThai = 2 " +
            "WHERE km.trangThai = 1 AND km.thoiGianKetThuc < CURRENT_TIMESTAMP")
    void capNhatKhuyenMaiHetHan();

    // Lấy khuyến mãi kèm theo chi tiết sản phẩm
    @Query("SELECT DISTINCT k FROM KhuyenMai k " +
            "LEFT JOIN FETCH k.khuyenMaiChitiets kmct " +
            "LEFT JOIN FETCH kmct.sanPhamChiTiet " +
            "WHERE k.id = :id")
    Optional<KhuyenMai> findByIdWithDetails(@Param("id") Long id);

    // Tìm khuyến mãi cần kích hoạt
    @Query("SELECT k FROM KhuyenMai k WHERE k.trangThai = 0 AND k.thoiGianBatDau <= :now")
    List<KhuyenMai> findKhuyenMaiToActivate(@Param("now") LocalDateTime now);

    // Tìm khuyến mãi cần kết thúc
    @Query("SELECT k FROM KhuyenMai k WHERE k.trangThai = 1 AND k.thoiGianKetThuc <= :now")
    List<KhuyenMai> findKhuyenMaiToExpire(@Param("now") LocalDateTime now);

    // Cập nhật trạng thái khuyến mãi
    @Modifying
    @Transactional
    @Query("UPDATE KhuyenMai k SET k.trangThai = :newStatus, k.ngayCapNhat = :now WHERE k.id = :id")
    void updateKhuyenMaiStatus(@Param("id") Long id, @Param("newStatus") Integer newStatus, @Param("now") LocalDateTime now);

    @Query("SELECT spct FROM KhuyenMaiChitiet kmct " +
            "JOIN kmct.sanPhamChiTiet spct " +
            "WHERE kmct.khuyenMai.id = :khuyenMaiId")
    List<SanPhamChiTiet> findSanPhamDaApDungByKhuyenMaiId(@Param("khuyenMaiId") Long khuyenMaiId);

}
