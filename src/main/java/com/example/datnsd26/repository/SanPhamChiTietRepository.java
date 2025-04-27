package com.example.datnsd26.repository;

import com.example.datnsd26.controller.response.PublicSanPhamResponse;
import com.example.datnsd26.models.SanPhamChiTiet;
import com.example.datnsd26.models.*;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


@Repository
public interface SanPhamChiTietRepository extends JpaRepository<SanPhamChiTiet, Integer> {

    @Query("SELECT spct FROM SanPhamChiTiet  spct WHERE spct.sanPham.id = :idSanPham AND spct.mauSac.id = :idMauSac")
    List<SanPhamChiTiet> findByIdSanPhamAndIdMauSac(Integer idSanPham, Integer idMauSac);

    @Query("FROM SanPhamChiTiet sp WHERE (sp.sanPham.tenSanPham like %:keyword% OR sp.maSanPhamChiTiet like %:keyword%) AND sp.soLuong >= 1 AND sp.trangThai = true")
    List<SanPhamChiTiet> findByNameOrCode(String keyword);

    // search theo biến thể sản phẩm
    @Query("SELECT spct FROM SanPhamChiTiet spct WHERE spct.sanPham = :sanPham " +
            "AND (:key IS NULL OR spct.sanPham.tenSanPham LIKE :key OR spct.maSanPhamChiTiet LIKE :key) " +
            "AND (:idThuongHieu IS NULL OR spct.thuongHieu.id = :idThuongHieu) " +
            "AND (:idDeGiay IS NULL OR spct.deGiay.id = :idDeGiay) " +
            "AND (:idKichCo IS NULL OR spct.kichCo.id = :idKichCo) " +
            "AND (:idMauSac IS NULL OR spct.mauSac.id = :idMauSac) " +
            "AND (:idChatLieu IS NULL OR spct.chatLieu.id = :idChatLieu) " +
            "AND (:gioiTinh IS NULL OR spct.gioiTinh = :gioiTinh) " +
            "AND (:trangThai IS NULL OR spct.trangThai = :trangThai)")
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
            "spct.trangThai=?9)")
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


    @Query("SELECT s FROM SanPhamChiTiet s WHERE NOT EXISTS " +
            "(SELECT k FROM KhuyenMaiChitiet k WHERE k.sanPhamChiTiet = s " +
            "AND k.khuyenMai.trangThai = 1 AND k.khuyenMai.thoiGianBatDau <= :now " +
            "AND k.khuyenMai.thoiGianKetThuc >= :now) " +
            "AND (LOWER(s.sanPham.tenSanPham) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(s.maSanPhamChiTiet) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<SanPhamChiTiet> findAvailableProductsWithSearch(
            @Param("searchTerm") String searchTerm,
            @Param("now") LocalDateTime now,
            Pageable pageable
    );

    @Query("FROM SanPhamChiTiet sp WHERE sp.soLuong > 0")
    Page<SanPhamChiTiet> findAvailableProducts(Pageable pageable);

    @Query("SELECT sp FROM SanPhamChiTiet sp WHERE sp.sanPham IS NOT NULL AND LOWER(sp.sanPham.tenSanPham) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<SanPhamChiTiet> findByTenSanPham(@Param("keyword") String keyword, Pageable pageable);


    Page<SanPhamChiTiet> findAllByTrangThaiTrue(Pageable pageable);

//    @Query("SELECT sp FROM SanPhamChiTiet sp " +
//            "WHERE (:keyword IS NULL OR sp.sanPham.tenSanPham LIKE %:keyword% OR sp.maSanPhamChiTiet LIKE %:keyword%) " +
//            "AND (:idMauSac IS NULL OR sp.mauSac.id = :idMauSac) " +
//            "AND (:idKichCo IS NULL OR sp.kichCo.id = :idKichCo)")
//    Page<SanPhamChiTiet> searchProducts(
//            @Param("keyword") String keyword,
//            @Param("idMauSac") Integer idMauSac,
//            @Param("idKichCo") Integer idKichCo,
//            Pageable pageable);



    @Query("SELECT p FROM SanPhamChiTiet p ORDER BY p.sanPham.tenSanPham ASC")
    List<PublicSanPhamResponse> findAllSortedByNameAsc();

    @Query("SELECT p FROM SanPhamChiTiet p ORDER BY p.sanPham.tenSanPham DESC")
    List<PublicSanPhamResponse> findAllSortedByNameDesc();

    @Query("SELECT p FROM SanPhamChiTiet p ORDER BY p.giaBan ASC")
    List<PublicSanPhamResponse> findAllSortedByPriceAsc();

    @Query("SELECT p FROM SanPhamChiTiet p ORDER BY p.giaBan DESC")
    List<PublicSanPhamResponse> findAllSortedByPriceDesc();


    @Query(value = """
    SELECT
        sp.ma_san_pham AS maSanPham,
        sp.ten_san_pham AS tenSanPham,
        kc.ten AS kichThuoc,
        spct.so_luong AS soLuongTon
    FROM san_pham_chi_tiet spct
    JOIN san_pham sp ON spct.id_san_pham = sp.id
    JOIN kich_co kc ON spct.id_kich_co = kc.id
    WHERE spct.so_luong < 5
    ORDER BY spct.so_luong ASC
    OFFSET :offset ROWS
    FETCH NEXT :pageSize ROWS ONLY
    """, nativeQuery = true)
    List<Object[]> getTopSanPhamSapHetNative(@Param("offset") int offset, @Param("pageSize") int pageSize);



    // Tìm theo sản phẩm và trạng thái
    List<SanPhamChiTiet> findBySanPhamAndTrangThaiTrue(SanPham sanPham);

    List<SanPhamChiTiet> findBySanPhamIdAndTrangThaiTrue(Integer sanPhamId);

    @Query("SELECT spct FROM SanPhamChiTiet spct WHERE " +
            "(:thuongHieuIds IS NULL OR spct.thuongHieu.id IN :thuongHieuIds) " +
            "AND (:chatLieuIds IS NULL OR spct.chatLieu.id IN :chatLieuIds) " +
            "AND (:deGiayIds IS NULL OR spct.deGiay.id IN :deGiayIds) " +
            "AND (:kichCoIds IS NULL OR spct.kichCo.id IN :kichCoIds) " +
            "AND (:mauSacIds IS NULL OR spct.mauSac.id IN :mauSacIds) " +
            "AND (:trangThai IS NULL OR spct.sanPham.trangThai = :trangThai)")
    List<SanPhamChiTiet> filterSanPham(
            @Param("thuongHieuIds") List<Long> thuongHieuIds,
            @Param("chatLieuIds") List<Long> chatLieuIds,
            @Param("deGiayIds") List<Long> deGiayIds,
            @Param("kichCoIds") List<Long> kichCoIds,
            @Param("mauSacIds") List<Long> mauSacIds,
            @Param("trangThai") Boolean trangThai
    );
    // Repository method trong SanPhamChiTietRepository.java
    List<SanPhamChiTiet> findBySanPham_Id(Integer sanPhamId);

    @Query("SELECT spct FROM SanPhamChiTiet spct " +
            "JOIN FETCH spct.sanPham sp " +
            "WHERE LOWER(sp.tenSanPham) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<SanPhamChiTiet> findBySanPhamTenSanPhamContainingIgnoreCase(@Param("keyword") String keyword);


}
