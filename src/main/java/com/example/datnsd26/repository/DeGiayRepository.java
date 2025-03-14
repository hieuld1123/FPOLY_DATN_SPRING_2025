package com.example.datnsd26.repository;

import com.example.datnsd26.models.DeGiay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface DeGiayRepository extends JpaRepository<DeGiay, Integer> {

    @Query( value = """
            SELECT dg FROM DeGiay dg WHERE (dg.ten LIKE?1) AND (?2 IS NULL OR dg.trangThai=?2)
            """)
    List<DeGiay> findByTenAndTrangThai(String ten, Boolean trangThai);

    List<DeGiay> findAllByOrderByNgayTaoDesc();

    @Query("SELECT dg FROM DeGiay dg WHERE dg.ten = :ten")
    List<DeGiay> findDeGiayByTen(@Param("ten") String ten);

    // search theo tên
    @Query("SELECT dg FROM DeGiay dg WHERE dg.ten = :ten AND dg.trangThai = true ")
    List<DeGiay> findDeGiayByTenAndTrangThaiFalse(@Param("ten") String ten);

    // hiển thị theo điều điều kiện trangthai=1
    @Query(nativeQuery = true, value = """
            SELECT * FROM de_giay Where trang_thai=1
            ORDER BY ngay_tao DESC
             """)
    List<DeGiay> getAll();

    // cập nhật trangthai=0
    @Modifying
    @Transactional
    @Query("UPDATE DeGiay d SET d.trangThai = false WHERE d.id = :id")
    void updateTrangThaiToFalseById(Integer id);

    boolean existsByTen(String ten);
}
