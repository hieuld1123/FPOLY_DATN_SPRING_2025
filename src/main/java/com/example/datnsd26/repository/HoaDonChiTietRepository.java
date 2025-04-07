package com.example.datnsd26.repository;

import com.example.datnsd26.Dto.SanPhamThongKeDTO;
import com.example.datnsd26.models.HoaDon;
import com.example.datnsd26.models.HoaDonChiTiet;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface HoaDonChiTietRepository extends JpaRepository<HoaDonChiTiet, Integer> {

    @Query("FROM HoaDonChiTiet ct WHERE ct.hoaDon.id = :invoiceId and ct.sanPhamChiTiet.id = :productId")
    Optional<HoaDonChiTiet> findHoaDonChiTietByIdAndProductId(int invoiceId, int productId);

    List<HoaDonChiTiet> findByHoaDon(HoaDon hoaDon);

    @Query("SELECT sp.id, sp.maSanPham, sp.tenSanPham, " +
            "SUM(hdct.soLuong), " +
            "SUM(hdct.soLuong * 1.0 * hdct.giaTienSauGiam) " +
            "FROM HoaDonChiTiet hdct " +
            "JOIN hdct.sanPhamChiTiet spct " +
            "JOIN spct.sanPham sp " +
            "WHERE hdct.hoaDon.trangThai = 'Hoàn thành' " +
            "GROUP BY sp.id, sp.maSanPham, sp.tenSanPham " +
            "ORDER BY SUM(hdct.soLuong) DESC")
    List<Object[]> findTopSanPhamThongKe(Pageable pageable);


    @Query("""
    SELECT SUM(hdct.soLuong * hdct.giaTienSauGiam)
    FROM HoaDonChiTiet hdct
    WHERE hdct.hoaDon.trangThai = 'Hoàn thành'
      AND hdct.hoaDon.ngayCapNhat BETWEEN :startDate AND :endDate
""")
    Float getDoanhThuTrongKhoang(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    @Query(value = """
    SELECT SUM(hdct.so_luong * hdct.gia_tien_sau_giam)
    FROM hoa_don_chi_tiet hdct
    JOIN hoa_don hd ON hdct.id_hoa_don = hd.id
    WHERE hd.trang_thai = 'Hoàn thành'
      AND CONVERT(date, hd.ngay_cap_nhat) = CONVERT(date, :today)
""", nativeQuery = true)
    Float getDoanhThuHomNay(@Param("today") Date today);

    @Query("""
    SELECT SUM(hdct.soLuong * hdct.giaTienSauGiam)
    FROM HoaDonChiTiet hdct
    WHERE hdct.hoaDon.trangThai = 'Hoàn thành'
      AND MONTH(hdct.hoaDon.ngayCapNhat) = :thang
      AND YEAR(hdct.hoaDon.ngayCapNhat) = :nam
""")
    Float getDoanhThuTheoThang(@Param("thang") int thang, @Param("nam") int nam);

    @Query("""
    SELECT SUM(hdct.soLuong * hdct.giaTienSauGiam)
    FROM HoaDonChiTiet hdct
    WHERE hdct.hoaDon.trangThai = 'Hoàn thành'
      AND FUNCTION('YEAR', hdct.hoaDon.ngayCapNhat) = :nam
""")
    Float getDoanhThuTheoNam(@Param("nam") int nam);

}
