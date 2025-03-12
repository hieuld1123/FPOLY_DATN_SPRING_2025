package com.example.datnsd26.repository;

import com.example.datnsd26.models.KichCo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface KichCoRepository extends JpaRepository<KichCo,Integer> {

    @Query( value = """
            SELECT kc FROM KichCo kc WHERE (kc.ten LIKE?1) AND (?2 IS NULL OR kc.trangthai=?2)
            """)
    List<KichCo> findByTenAndTrangthai(String ten, Boolean trangthai);

    boolean existsByTen(String ten);

    List<KichCo> getKichCoByTenOrTrangthai(String ten, Boolean trangthai);

    List<KichCo> findAllByOrderByNgaytaoDesc();

    KichCo findByTen(String ten);

    @Query("SELECT kc FROM KichCo kc WHERE kc.ten = :ten AND kc.trangthai = true ")
    List<KichCo> findKichCoByTenAndTrangThaiFalse(@Param("ten") String ten);

    @Query(nativeQuery = true, value = """
            SELECT * FROM KichCo Where TrangThai=1
            ORDER BY NgayTao DESC
             """)
    List<KichCo> getAll();

    @Modifying
    @Transactional
    @Query("UPDATE KichCo kc SET kc.trangthai = false WHERE kc.id = :id")
    void updateTrangThaiToFalseById(Integer id);
}
