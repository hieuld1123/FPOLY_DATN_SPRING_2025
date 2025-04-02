package com.example.datnsd26.repository;

import com.example.datnsd26.models.HoaDon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface HoaDonRepository extends JpaRepository<HoaDon, Integer> {

    @Query("SELECT MAX(h.maHoaDon) FROM HoaDon h")
    String findLastMaHoaDon();

    @Query("FROM HoaDon hd WHERE hd.trangThai = :status")
    List<HoaDon> findAllInvoiceByStatus(@Param("status") String status);

    @Query("SELECT COALESCE(SUM(h.tongTien), 0) FROM HoaDon h WHERE CAST(h.ngayTao AS date) = CAST(:today AS date) AND h.trangThai = 'Hoàn thành'")
    Float getDoanhThuHomNay(@Param("today") Date today);

    @Query("SELECT COUNT(h) FROM HoaDon h WHERE CAST(h.ngayTao AS date) = CAST(:today AS date) AND h.trangThai = 'Hoàn thành'")
    Long getSoDonHangMoi(@Param("today") Date today);

    @Query("SELECT COUNT(h) FROM HoaDon h WHERE CAST(h.ngayTao AS date) = CAST(:today AS date) AND h.trangThai = 'Đã hủy'")
    Long getSoDonDaHuy(@Param("today") Date today);

    Optional<HoaDon> findHoaDonByMaHoaDon(String maHoaDon);

    Optional<HoaDon> findByMaHoaDonAndSdtNguoiNhan(String maHoaDon, String sdtNguoiNhan);

}
