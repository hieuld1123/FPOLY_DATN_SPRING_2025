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
            SELECT cl FROM ChatLieu cl WHERE (cl.ten LIKE?1) AND (?2 IS NULL OR cl.trangThai=?2)
            """)
    List<ChatLieu> findByTenAndTrangThai(String ten, Boolean trangThai);

    @Query("SELECT c FROM ChatLieu c WHERE c.ten LIKE %?1% OR c.trangThai = ?2")
    List<ChatLieu> findByTenVaTrangThai(String ten, Boolean trangThai);

    List<ChatLieu> getChatLieuByTenOrTrangThai(String ten, Boolean trangThai);

    List<ChatLieu> findAllByOrderByNgayTaoDesc();


    @Query("SELECT cl FROM ChatLieu cl WHERE cl.ten = :ten AND cl.trangThai = true ")
    List<ChatLieu> findChatLieuByTenAndTrangThaiFalse(@Param("ten") String ten);

    @Query(nativeQuery = true, value = """
            SELECT * FROM chat_lieu Where trang_thai=1
            ORDER BY ngay_tao DESC
             """)
    List<ChatLieu> getAll();

    @Modifying
    @Transactional
    @Query("UPDATE ChatLieu cl SET cl.trangThai = false WHERE cl.id = :id")
    void updateTrangThaiToFalseById(Integer id);

}
