package com.example.datnsd26.repository;

import com.example.datnsd26.models.HinhAnh;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface HinhAnhRepository extends JpaRepository<HinhAnh,Integer> {

    @Transactional
    @Modifying
    @Query("DELETE FROM HinhAnh h WHERE h.id = :id AND h.sanPhamChiTiet.id = :idSanPhamChiTiet")
    void deleteByIdAndSanPhamChiTietId(Integer id, Integer idSanPhamChiTiet);

}
