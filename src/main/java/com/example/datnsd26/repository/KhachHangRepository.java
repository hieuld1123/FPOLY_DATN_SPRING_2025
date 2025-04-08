package com.example.datnsd26.repository;

import com.example.datnsd26.models.KhachHang;
import com.example.datnsd26.models.NhanVien;
import com.example.datnsd26.models.TaiKhoan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KhachHangRepository extends JpaRepository<KhachHang, Integer> {
    Optional<KhachHang> findByMaKhachHang(String maKhachHang);

    @Query(value = "SELECT kh FROM KhachHang kh WHERE " +
            "(kh.tenKhachHang LIKE %:search% " +
            "OR kh.taiKhoan.sdt LIKE %:search% " +
            "OR kh.maKhachHang LIKE %:search% " +
            "OR kh.tenKhachHang LIKE %:search% " +
            "OR kh.taiKhoan.email LIKE %:search%) " +
            "AND (:status IS NULL OR kh.trangThai = :status) " +
            "ORDER BY kh.id DESC")
    Page<KhachHang> findByTenSdtMaTT(@Param("search") String tenSdtMa,
                                    @Param("status") Boolean status,
                                    Pageable pageable);

    @Query("FROM KhachHang kh WHERE (kh.tenKhachHang like %:keyword%) OR (kh.maKhachHang like %:keyword%) OR (kh.taiKhoan.sdt like %:keyword%)")
    List<KhachHang> findByNameOrCodeOrPhone(String keyword);

    @Query("FROM KhachHang kh WHERE kh.taiKhoan.email LIKE :email")
    Optional<KhachHang> findByEmail(String email);
    KhachHang findByTaiKhoan(TaiKhoan taiKhoan);
}
