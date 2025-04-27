package com.example.datnsd26.repository;

import com.example.datnsd26.models.HoaDon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface HoaDonRepository extends JpaRepository<HoaDon, Integer> {

    @Query("SELECT MAX(h.maHoaDon) FROM HoaDon h")
    String findLastMaHoaDon();

    @Query("FROM HoaDon hd WHERE hd.trangThai = :status")
    List<HoaDon> findAllInvoiceByStatus(@Param("status") String status);

    @Query("""
    SELECT COUNT(hd)
    FROM HoaDon hd
    WHERE hd.trangThai = 'Hoàn thành'
      AND CAST(hd.ngayCapNhat AS date) = :today
""")
    Long getSoDonHoanThanh(@Param("today") Date today);

    @Query("""
    SELECT COUNT(hd)
    FROM HoaDon hd
    WHERE hd.trangThai = 'Hoàn thành'
      AND hd.ngayCapNhat BETWEEN :startDate AND :endDate
""")
    Long countDonHoanThanhTrongKhoang(@Param("startDate") Date startDate,
                                      @Param("endDate") Date endDate);


    @Query(value = """
    SELECT COUNT(*) FROM hoa_don 
    WHERE trang_thai = N'Đã hủy' 
      AND CAST(ngay_cap_nhat AS date) = CAST(:today AS date)
""", nativeQuery = true)
    Long countDonDaHuy(@Param("today") Date today);


    @Query(value = """
    SELECT COUNT(*) FROM hoa_don 
    WHERE trang_thai = N'Đã hủy'
      AND ngay_cap_nhat BETWEEN :startDate AND :endDate
""", nativeQuery = true)
    Long countDonHuyTrongKhoang(@Param("startDate") Date startDate,
                                      @Param("endDate") Date endDate);

    @Query("""
    SELECT COUNT(hd)
    FROM HoaDon hd
    WHERE hd.trangThai = 'Hoàn thành'
      AND FUNCTION('MONTH', hd.ngayCapNhat) = :thang
      AND FUNCTION('YEAR', hd.ngayCapNhat) = :nam
""")
    Long countDonHoanThanhTheoThangNam(@Param("thang") int thang, @Param("nam") int nam);
    @Query("""
    SELECT COUNT(hd)
    FROM HoaDon hd
    WHERE hd.trangThai = 'Hoàn thành'
      AND FUNCTION('YEAR', hd.ngayCapNhat) = :nam
""")
    Long countDonHoanThanhTheoNam(@Param("nam") int nam);

    @Query(value = """
    SELECT COUNT(*) FROM hoa_don 
    WHERE trang_thai = N'Đã hủy' 
      AND MONTH(ngay_cap_nhat) = :thang 
      AND YEAR(ngay_cap_nhat) = :nam
""", nativeQuery = true)
    Long countDonHuyTheoThangVaNam(@Param("thang") int thang, @Param("nam") int nam);

    @Query(value = """
    SELECT COUNT(*) FROM hoa_don 
    WHERE trang_thai = N'Đã hủy' 
      AND YEAR(ngay_cap_nhat) = :nam
""", nativeQuery = true)
    Long countDonHuyTheoNam(@Param("nam") int nam);

    @Query(value = "SELECT COALESCE(SUM(hdct.so_luong * spct.gia_ban - COALESCE(hd.giam_gia, 0)), 0) " +
            "FROM hoa_don hd " +
            "JOIN hoa_don_chi_tiet hdct ON hd.id = hdct.id_hoa_don " +
            "JOIN san_pham_chi_tiet spct ON spct.id = hdct.id_san_pham_chi_tiet " +
            "WHERE hd.trang_thai = 'Hoàn thành' " +
            "AND hd.ngay_cap_nhat >= :batDau " +
            "AND hd.ngay_cap_nhat < :ketThuc", nativeQuery = true)
    double tinhDoanhThuTheoNgay(@Param("batDau") LocalDateTime batDau,
                                @Param("ketThuc") LocalDateTime ketThuc);

    @Query(value = """
    SELECT DATEPART(HOUR, hd.ngay_cap_nhat) AS gio, 
           SUM(hd.tong_tien - COALESCE(hd.giam_gia, 0)) AS doanh_thu
    FROM hoa_don hd
    WHERE CAST(hd.ngay_cap_nhat AS DATE) = :ngay
      AND hd.trang_thai = 'Hoàn thành'
    GROUP BY DATEPART(HOUR, hd.ngay_cap_nhat)
    ORDER BY gio
    """, nativeQuery = true)
    List<Object[]> tongDoanhThuTheoGioTrongNgay(@Param("ngay") LocalDate ngay);




    Optional<HoaDon> findHoaDonByMaHoaDon(String maHoaDon);

    Optional<HoaDon> findByMaHoaDonAndSdtNguoiNhan(String maHoaDon, String sdtNguoiNhan);

    List<HoaDon> findByKhachHang_TaiKhoan_EmailOrderByNgayTaoDesc(String email);

}
