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
            SELECT ms FROM MauSac ms WHERE (ms.ten LIKE?1) AND (?2 IS NULL OR ms.trangthai=?2)
            """)
    List<MauSac> findByTenAndTrangthai(String ten, Boolean trangthai);

    boolean existsByTen(String ten);


    List<MauSac> findAllByOrderByNgaytaoDesc();

    @Query("SELECT ms FROM MauSac ms WHERE ms.ten = :ten AND ms.trangthai = true ")
    List<MauSac> findMauSacByTenAndTrangThaiFalse(@Param("ten") String ten);

    @Query(nativeQuery = true, value = """
            SELECT * FROM MauSac Where TrangThai=1
            ORDER BY NgayTao DESC
             """)
    List<MauSac> getAll();

    @Modifying
    @Transactional
    @Query("UPDATE MauSac ms SET ms.trangthai = false WHERE ms.id = :id")
    void updateTrangThaiToFalseById(Integer id);

}