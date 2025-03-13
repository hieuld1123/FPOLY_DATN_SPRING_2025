package com.example.datnsd26.repository;

import com.example.datnsd26.models.DiaChi;
import com.example.datnsd26.models.KhachHang;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiaChiRepository extends JpaRepository<DiaChi,Integer> {
    List<DiaChi> findByKhachHangId(Integer khachHangId);
    void deleteById(Integer id);

    void deleteById(Integer id);

    @Modifying
    @Query("DELETE FROM DiaChi d WHERE d.khachHang.id = :khachHangId")
    void deleteAllByKhachHangId(@Param("khachHangId") Integer khachHangId);

}
