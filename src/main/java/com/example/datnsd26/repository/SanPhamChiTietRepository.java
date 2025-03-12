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
    @Query("SELECT spct FROM SanPhamChiTiet spct WHERE spct.sanpham = :sanPham " +
            "AND (:key IS NULL OR spct.sanpham.tensanpham LIKE :key OR spct.masanphamchitiet LIKE :key) " +
            "AND (:idThuongHieu IS NULL OR spct.thuonghieu.id = :idThuongHieu) " +
            "AND (:idDeGiay IS NULL OR spct.degiay.id = :idDeGiay) " +
            "AND (:idKichCo IS NULL OR spct.kichco.id = :idKichCo) " +
            "AND (:idMauSac IS NULL OR spct.mausac.id = :idMauSac) " +
            "AND (:idChatLieu IS NULL OR spct.chatlieu.id = :idChatLieu) " +
            "AND (:gioiTinh IS NULL OR spct.gioitinh = :gioiTinh) " +
            "AND (:trangThai IS NULL OR spct.sanpham.trangthai = :trangThai)")
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

            SELECT s FROM SanPhamChiTiet s WHERE  s.mausac=?1 AND s.kichco=?2 AND s.thuonghieu=?3 AND 
            s.chatlieu=?4 AND  s.degiay=?5 AND  s.sanpham=?6
            """)
    SanPhamChiTiet findSPCT(MauSac mauSac, KichCo kichCo, ThuongHieu thuongHieu, ChatLieu chatLieu, DeGiay deGiay, SanPham sanPham);

    // tìm theo sản phẩm
    List<SanPhamChiTiet> findBySanpham(SanPham sanPham);


    Boolean existsByMasanphamchitiet(String ma);


    @Query("SELECT c FROM SanPhamChiTiet c WHERE c.masanphamchitiet like %?1%")
    List<SanPhamChiTiet> searchSPCTtheoMa(String masp);


    @Query("SELECT c FROM SanPhamChiTiet c WHERE c.sanpham.tensanpham like %?1% and c.chatlieu.ten like %?2% and c.thuonghieu.ten like %?3% and c.degiay.ten like %?4% and c.kichco.ten like %?5% and c.mausac.ten like %?6% and c.gioitinh = ?7 and c.giatien <= ?8 and c.soluong >0")
    List<SanPhamChiTiet> searchSPCT(String tenSp, String chatlieu,
                                    String ThuongHieu, String De,
                                    String KichCo, String MauSac,
                                    Boolean gioitinh, BigDecimal gia);

    // dùng để lấy id cao nhất
    @Query(value = "SELECT MAX(spct.id) FROM SanPhamChiTiet spct")
    Integer findMaxIdSPCT();

    //dùng cho search các thuộc tính sản phẩm chi tiết
    @Query("SELECT spct FROM SanPhamChiTiet spct WHERE (spct.sanpham.tensanpham LIKE ?1 OR spct.masanphamchitiet LIKE ?2) AND (?3 IS NULL OR spct.thuonghieu.id=?3) " +
            "AND (?4 IS NULL OR " + " spct.degiay.id=?4) AND (?5 IS NULL OR spct.kichco.id=?5) AND (?6 IS NULL OR spct.mausac.id=?6)" +
            "AND (?7 IS NULL OR spct.chatlieu.id=?7) AND (?8 IS NULL OR spct.gioitinh=?8) AND (?9 IS NULL OR spct.sanpham.trangthai=?9)")
    List<SanPhamChiTiet> search(String key, String maSPCT, Integer idthuonghieu, Integer iddegiay, Integer idkichco, Integer idmausac, Integer idchatlieu, Boolean gioitinh, Boolean trangthai);


    @Query("SELECT spct FROM SanPhamChiTiet spct WHERE spct.sanpham.id = :Id")
    List<SanPhamChiTiet> findBySanPhamId(@Param("Id") Integer Id);

    //update số lượng và giá tiền
    @Transactional
    @Modifying
    @Query(value = "UPDATE SanPhamChiTiet SET soluong = :soluong, giatien = :giatien WHERE id = :id", nativeQuery = true)
    void updateSoLuongVaGiaTienById(@Param("id") Integer id, @Param("soluong") Integer soLuong, @Param("giatien") BigDecimal giaTien);

    // lấy id by sản phẩm
    @Query("SELECT s.sanpham.id FROM SanPhamChiTiet s WHERE s.id = :spctId")
    Integer findIdBySanpham(Integer spctId);

    Page<SanPhamChiTiet> findAllBySoluongGreaterThan(Integer soluong, Pageable p);



}