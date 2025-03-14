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
            SELECT kc FROM KichCo kc WHERE (kc.ten LIKE?1) AND (?2 IS NULL OR kc.trangThai=?2)
            """)
    List<KichCo> findByTenAndTrangThai(String ten, Boolean trangThai);

    boolean existsByTen(String ten);

    List<KichCo> getKichCoByTenOrTrangThai(String ten, Boolean trangThai);

    List<KichCo> findAllByOrderByNgayTaoDesc();

    KichCo findByTen(String ten);

    @Query("SELECT kc FROM KichCo kc WHERE kc.ten = :ten AND kc.trangThai = true ")
    List<KichCo> findKichCoByTenAndTrangThaiFalse(@Param("ten") String ten);

    @Query(nativeQuery = true, value = """
            SELECT * FROM kich_co Where trang_thai=1
            ORDER BY ngay_tao DESC
             """)
    List<KichCo> getAll();

    @Modifying
    @Transactional
    @Query("UPDATE KichCo kc SET kc.trangThai = false WHERE kc.id = :id")
    void updateTrangThaiToFalseById(Integer id);
}
