package com.example.datnsd26.repository;

import com.example.datnsd26.models.MauSac;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface MauSacRepository extends JpaRepository<MauSac, Integer> {

    @Query( value = """
            SELECT ms FROM MauSac ms WHERE (ms.ten LIKE?1) AND (?2 IS NULL OR ms.trangThai=?2)
            """)
    List<MauSac> findByTenAndTrangThai(String ten, Boolean trangThai);

    boolean existsByTen(String ten);


    List<MauSac> findAllByOrderByNgayTaoDesc();

    @Query("SELECT ms FROM MauSac ms WHERE ms.ten = :ten AND ms.trangThai = true ")
    List<MauSac> findMauSacByTenAndTrangThaiFalse(@Param("ten") String ten);

    @Query(nativeQuery = true, value = """
            SELECT * FROM mau_sac Where trang_thai=1
            ORDER BY ngay_tao DESC
             """)
    List<MauSac> getAll();

    @Modifying
    @Transactional
    @Query("UPDATE MauSac ms SET ms.trangThai = false WHERE ms.id = :id")
    void updateTrangThaiToFalseById(Integer id);

    @Query("SELECT DISTINCT spct.mauSac FROM SanPhamChiTiet spct WHERE spct.sanPham.id = :idSanPham")
    List<MauSac> findBySanPham_Id(@Param("idSanPham") Integer idSanPham);


}