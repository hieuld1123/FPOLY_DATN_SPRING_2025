package com.example.datnsd26.repository;

import com.example.datnsd26.models.SanPham;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface SanPhamRepositoty extends JpaRepository<SanPham, Integer> {

    @Query(nativeQuery = true, value = """
            SELECT * FROM SanPham WHERE TrangThai=1
               ORDER BY NgayTao DESC
            """)
    List<SanPham> getAllByNgayTao();

    // tìm id lớn nhất bên sp
    @Query(value = "SELECT MAX(s.id) FROM SanPham s")
    Integer findMaxIdSP();

    SanPham findFirstByOrderByNgaytaoDesc();

    boolean existsByTensanpham(String tensanpham);

    // search bên sp
    @Query("SELECT sp.id, sp.tensanpham, sp.ngaytao,SUM(spct.soluong) as tongSoLuong ,sp.trangthai,sp.masanpham " +
            "FROM SanPham sp " +
            "JOIN sp.spct spct " +
            "WHERE (sp.masanpham LIKE?1 OR sp.tensanpham LIKE ?2)AND(?3 IS NULL OR sp.trangthai=?3) " +
            "GROUP BY sp.id, sp.tensanpham, sp.ngaytao, sp.trangthai, sp.masanpham " +
            "ORDER BY sp.ngaytao DESC, tongSoLuong DESC")
    List<Object[]> findByMasanphamAndTenSanPhamAndTrangThai(String masanpham, String key, Boolean trangthai);

    // các sp mới nhất bên sp
    @Query("SELECT sp.id, sp.tensanpham, sp.ngaytao, SUM(spct.soluong) AS tongSoLuong,sp.trangthai, sp.masanpham " +
            "FROM SanPham sp JOIN sp.spct spct " +
            "GROUP BY sp.id, sp.tensanpham, sp.ngaytao, sp.trangthai, sp.masanpham " +
            "ORDER BY sp.ngaytao DESC, tongSoLuong DESC")
    List<Object[]> findProductsWithTotalQuantityOrderByDateDesc();

    //lấy id và tên sản phẩm
    @Query("SELECT sp.id, sp.tensanpham FROM SanPham sp WHERE sp.id = :id")
    List<Object[]> findByIdUpdatTenSP(@Param("id") Integer id);

}