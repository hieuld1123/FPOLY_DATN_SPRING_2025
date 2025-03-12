package com.example.datnsd26.repository;

import com.example.datnsd26.models.ChatLieu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ChatLieuRepository extends JpaRepository<ChatLieu, Integer> {
    boolean existsByTen(String ten);
    @Query(value = """
            SELECT cl FROM ChatLieu cl WHERE (cl.ten LIKE?1) AND (?2 IS NULL OR cl.trangthai=?2)
            """)
    List<ChatLieu> findByTenAndTrangthai(String ten, Boolean trangthai);

    @Query("SELECT c FROM ChatLieu c WHERE c.ten LIKE %?1% OR c.trangthai = ?2")
    List<ChatLieu> findByTenVaTrangThai(String ten, Boolean trangthai);

    List<ChatLieu> getChatLieuByTenOrTrangthai(String ten, Boolean trangthai);

    List<ChatLieu> findAllByOrderByNgaytaoDesc();


    @Query("SELECT cl FROM ChatLieu cl WHERE cl.ten = :ten AND cl.trangthai = true ")
    List<ChatLieu> findChatLieuByTenAndTrangThaiFalse(@Param("ten") String ten);

    @Query(nativeQuery = true, value = """
            SELECT * FROM ChatLieu Where TrangThai=1
            ORDER BY NgayTao DESC
             """)
    List<ChatLieu> getAll();

    @Modifying
    @Transactional
    @Query("UPDATE ChatLieu cl SET cl.trangthai = false WHERE cl.id = :id")
    void updateTrangThaiToFalseById(Integer id);

}
