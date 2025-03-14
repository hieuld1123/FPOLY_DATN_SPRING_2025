package com.example.datnsd26.repository;

import com.example.datnsd26.models.SanPhamChiTiet;
import com.example.datnsd26.models.*;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;


@Repository
public interface SanPhamChiTietRepository extends JpaRepository<SanPhamChiTiet, Integer> {

    // search theo biến thể sản phẩm
    @Query("SELECT spct FROM SanPhamChiTiet spct WHERE spct.sanPham = :sanPham " +
            "AND (:key IS NULL OR spct.sanPham.tenSanPham LIKE :key OR spct.maSanPhamChiTiet LIKE :key) " +
            "AND (:idThuongHieu IS NULL OR spct.thuongHieu.id = :idThuongHieu) " +
            "AND (:idDeGiay IS NULL OR spct.deGiay.id = :idDeGiay) " +
            "AND (:idKichCo IS NULL OR spct.kichCo.id = :idKichCo) " +
            "AND (:idMauSac IS NULL OR spct.mauSac.id = :idMauSac) " +
            "AND (:idChatLieu IS NULL OR spct.chatLieu.id = :idChatLieu) " +
            "AND (:gioiTinh IS NULL OR spct.gioiTinh = :gioiTinh) " +
            "AND (:trangThai IS NULL OR spct.sanPham.trangThai = :trangThai)")
    List<SanPhamChiTiet> searchBySanPham(
            @Param("sanPham") SanPham sanPham,
            @Param("key") String key,
            @Param("idThuongHieu") Integer idThuongHieu,
            @Param("idDeGiay") Integer idDeGiay,
            @Param("idKichCo") Integer idKichCo,
            @Param("idMauSac") Integer idMauSac,
            @Param("idChatLieu") Integer idChatLieu,
            @Param("gioiTinh") Boolean gioiTinh,
            @Param("trangThai") Boolean trangThai
    );


    @Query(value = """

            SELECT s FROM SanPhamChiTiet s WHERE  
            s.mauSac=?1 AND 
            s.kichCo=?2 AND 
            s.thuongHieu=?3 AND 
            s.chatLieu=?4 AND  
            s.deGiay=?5 AND  
            s.sanPham=?6
            """)
    SanPhamChiTiet findSPCT(MauSac mauSac, KichCo kichCo, ThuongHieu thuongHieu, ChatLieu chatLieu, DeGiay deGiay, SanPham sanPham);

    // tìm theo sản phẩm
    List<SanPhamChiTiet> findBySanPham(SanPham sanPham);


    Boolean existsByMaSanPhamChiTiet(String ma);


    @Query("SELECT c FROM SanPhamChiTiet c WHERE c.maSanPhamChiTiet like %?1%")
    List<SanPhamChiTiet> searchSPCTtheoMa(String masp);


    @Query("SELECT c FROM SanPhamChiTiet c WHERE " +
            "c.sanPham.tenSanPham like %?1% and " +
            "c.chatLieu.ten like %?2% and " +
            "c.thuongHieu.ten like %?3% and " +
            "c.deGiay.ten like %?4% and " +
            "c.kichCo.ten like %?5% and " +
            "c.mauSac.ten like %?6% and " +
            "c.gioiTinh = ?7 and " +
            "c.giaBan <= ?8 and " +
            "c.soLuong >0")
    List<SanPhamChiTiet> searchSPCT(String tenSp, String chatlieu,
                                    String ThuongHieu, String De,
                                    String KichCo, String MauSac,
                                    Boolean gioitinh, BigDecimal gia);

    // dùng để lấy id cao nhất
    @Query(value = "SELECT MAX(spct.id) FROM SanPhamChiTiet spct")
    Integer findMaxIdSPCT();

    //dùng cho search các thuộc tính sản phẩm chi tiết
    @Query("SELECT spct FROM SanPhamChiTiet spct WHERE (" +
            "spct.sanPham.tenSanPham LIKE ?1 OR " +
            "spct.maSanPhamChiTiet LIKE ?2) AND (?3 IS NULL OR " +
            "spct.thuongHieu.id=?3) " +
            "AND (?4 IS NULL OR " + " " +
            "spct.deGiay.id=?4) AND (?5 IS NULL OR " +
            "spct.kichCo.id=?5) AND (?6 IS NULL OR " +
            "spct.mauSac.id=?6)" +
            "AND (?7 IS NULL OR " +
            "spct.chatLieu.id=?7) AND (?8 IS NULL OR " +
            "spct.gioiTinh=?8) AND (?9 IS NULL OR " +
            "spct.sanPham.trangThai=?9)")
    List<SanPhamChiTiet> search(String key, String maSPCT, Integer idthuonghieu, Integer iddegiay, Integer idkichco, Integer idmausac, Integer idchatlieu, Boolean gioitinh, Boolean trangthai);


    @Query("SELECT spct FROM SanPhamChiTiet spct WHERE spct.sanPham.id = :Id")
    List<SanPhamChiTiet> findBySanPhamId(@Param("Id") Integer Id);

    //update số lượng và giá tiền
    @Transactional
    @Modifying
    @Query(value = "UPDATE san_pham_chi_tiet SET so_luong = :soluong, gia_ban = :giatien WHERE id = :id", nativeQuery = true)
    void updateSoLuongVaGiaTienById(@Param("id") Integer id, @Param("soluong") Integer soLuong, @Param("giatien") BigDecimal giaTien);

    // lấy id by sản phẩm
    @Query("SELECT s.sanPham.id FROM SanPhamChiTiet s WHERE s.id = :spctId")
    Integer findIdBySanpham(Integer spctId);

    Page<SanPhamChiTiet> findAllBySoLuongGreaterThan(Integer soluong, Pageable p);



}