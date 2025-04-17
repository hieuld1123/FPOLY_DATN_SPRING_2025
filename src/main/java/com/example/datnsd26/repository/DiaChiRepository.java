package com.example.datnsd26.repository;

import com.example.datnsd26.controller.request.AddressRequest;
import com.example.datnsd26.models.DiaChi;
import com.example.datnsd26.models.KhachHang;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DiaChiRepository extends JpaRepository<DiaChi,Integer> {
    List<DiaChi> findByKhachHangId(Integer khachHangId);
    void deleteById(Integer id);

    @Modifying
    @Transactional
    @Query("DELETE FROM DiaChi d WHERE d.khachHang.id = :khachHangId")
    void deleteAllByKhachHangId(@Param("khachHangId") Integer khachHangId);


    @Query("FROM DiaChi dc WHERE " +
            "(:customerId IS NULL OR dc.khachHang.id = :customerId) " +
            "AND (:province IS NULL OR dc.tinh LIKE %:province%) " +
            "AND (:district IS NULL OR dc.huyen LIKE %:district%) " +
            "AND (:ward IS NULL OR dc.xa LIKE %:ward%) " +
            "AND (:addressDetail IS NULL OR dc.diaChiCuThe LIKE %:addressDetail%)")
    Optional<DiaChi> findFirstByValues(
            @Param("customerId") Integer customerId,
            @Param("province") String province,
            @Param("district") String district,
            @Param("ward") String ward,
            @Param("addressDetail") String addressDetail
    );

    List<DiaChi> findByKhachHang(KhachHang khachHang);
}
