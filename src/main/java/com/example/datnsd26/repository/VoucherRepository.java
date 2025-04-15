package com.example.datnsd26.repository;

import com.example.datnsd26.models.Voucher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Long> {
    boolean existsByMaVoucher(String maVoucher);
    Optional<Voucher> findByMaVoucher(String maVoucher);
    List<Voucher> findByTenVoucher(String tenVoucher);
    List<Voucher> findByTenVoucherAndIdNot(String tenVoucher, Long id);
    List<Voucher> findByTrangThai(Integer trangThai);
    List<Voucher> findByNgayBatDauBeforeAndNgayKetThucAfter(LocalDateTime ngayBatDau, LocalDateTime now);

    // Tìm voucher đang chưa bắt đầu (0) nhưng đã đến hạn kích hoạt
    @Query("SELECT v FROM Voucher v WHERE v.trangThai = 0 AND v.ngayBatDau <= :now")
    List<Voucher> findVouchersToActivate(@Param("now") LocalDateTime now);

    // Tìm voucher đang hoạt động (1) nhưng đã đến hạn hết hạn
    @Query("SELECT v FROM Voucher v WHERE v.trangThai = 1 AND v.ngayKetThuc <= :now")
    List<Voucher> findVouchersToExpire(@Param("now") LocalDateTime now);

    @Modifying
    @Transactional
    @Query("UPDATE Voucher v SET v.trangThai = :newStatus, v.ngayCapNhat = :now WHERE v.id = :id")
    void updateVoucherStatusById(@Param("id") Long id, @Param("newStatus") Integer newStatus, @Param("now") LocalDateTime now);

    @Query("SELECT v FROM Voucher v WHERE " +
            "(:maVoucher = '' OR v.maVoucher LIKE %:maVoucher%) AND " +
            "(:tenVoucher = '' OR v.tenVoucher LIKE %:tenVoucher%) AND " +
            "(:trangThai IS NULL OR v.trangThai = :trangThai) AND " +
            "(:ngayBatDau IS NULL OR v.ngayBatDau >= :ngayBatDau) AND " +
            "(:ngayKetThuc IS NULL OR v.ngayKetThuc <= :ngayKetThuc)")
    Page<Voucher> searchVouchers(@Param("maVoucher") String maVoucher,
                                 @Param("tenVoucher") String tenVoucher,
                                 @Param("trangThai") Integer trangThai,
                                 @Param("ngayBatDau") LocalDateTime ngayBatDau,
                                 @Param("ngayKetThuc") LocalDateTime ngayKetThuc,
                                 Pageable pageable);

    List<Voucher> findByTrangThaiNot(Integer trangThai);

//    @Modifying
//    @Query("UPDATE Voucher v SET v.trangThai = :trangThai, v.ngayCapNhat = :ngayCapNhat WHERE v.id = :id")
//    void updateTrangThai(@Param("id") Long id, @Param("trangThai") Integer trangThai, @Param("ngayCapNhat") LocalDateTime ngayCapNhat);
}

