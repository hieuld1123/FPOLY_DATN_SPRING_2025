package com.example.datnsd26.services.cart;

import com.example.datnsd26.models.GioHang;
import com.example.datnsd26.models.GioHangChiTiet;
import com.example.datnsd26.models.SanPhamChiTiet;
import com.example.datnsd26.models.TaiKhoan;
import com.example.datnsd26.repository.*;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GioHangService {
    @Autowired
    private GioHangRepository gioHangRepository;

    @Autowired
    private GioHangChiTietRepository gioHangChiTietRepository;

    @Autowired
    private SanPhamChiTietRepository sanPhamChiTietRepository;

    @Autowired
    private TaiKhoanRepository taiKhoanRepository;

    @Autowired
    private KhachHangRepository khachHangRepository;

    @Autowired
    private HttpSession session;

    public GioHang getGioHangHienTai(Authentication auth) {
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            UserDetails userDetails = (UserDetails) auth.getPrincipal();
            TaiKhoan taiKhoan = taiKhoanRepository.findByEmail(userDetails.getUsername()).orElse(null);

            if (taiKhoan != null) {
                return gioHangRepository.findByTaiKhoan(taiKhoan).orElseGet(() -> taoGioHangChoTaiKhoan(taiKhoan));
            }
        }

        // Nếu không đăng nhập, sử dụng guestId từ session
        String guestId = (String) session.getAttribute("guestId");
        if (guestId == null) {
            guestId = UUID.randomUUID().toString();
            session.setAttribute("guestId", guestId);
        }
        String finalGuestId = guestId;
        return gioHangRepository.findByIdKhachVangLai(guestId).orElseGet(() -> taoGioHangChoGuest(finalGuestId));
    }


    private GioHang taoGioHangChoTaiKhoan(TaiKhoan taiKhoan) {
        GioHang gioHang = new GioHang();
        gioHang.setTaiKhoan(taiKhoan);
        gioHang.setNgayTao(new Date());
        return gioHangRepository.save(gioHang);
    }

    private GioHang taoGioHangChoGuest(String guestId) {
        GioHang gioHang = new GioHang();
        gioHang.setIdKhachVangLai(guestId);
        gioHang.setNgayTao(new Date());
        return gioHangRepository.save(gioHang);
    }

    public void themSanPhamVaoGioHang(Integer sanPhamChiTietId, int soLuongMoiThem, Authentication auth) {
        GioHang gioHang = getGioHangHienTai(auth);

        SanPhamChiTiet spct = sanPhamChiTietRepository.findById(sanPhamChiTietId).orElseThrow();

        // Tổng số lượng đã có trong giỏ
        GioHangChiTiet gioHangChiTiet = gioHangChiTietRepository.findByGioHangAndSanPhamChiTiet(gioHang, spct).orElse(null);
        int daCoTrongGio = gioHangChiTiet != null ? gioHangChiTiet.getSoLuong() : 0;

        int tongMuonThem = daCoTrongGio + soLuongMoiThem;

        if (tongMuonThem > spct.getSoLuong()) {
            throw new RuntimeException("Vượt quá số lượng tồn kho");
        }

        if (gioHangChiTiet == null) {
            gioHangChiTiet = new GioHangChiTiet();
            gioHangChiTiet.setGioHang(gioHang);
            gioHangChiTiet.setSanPhamChiTiet(spct);
            gioHangChiTiet.setSoLuong(soLuongMoiThem);
        } else {
            gioHangChiTiet.setSoLuong(tongMuonThem);
        }

        gioHangChiTietRepository.save(gioHangChiTiet);
    }


    public void capNhatSoLuongSanPham(Integer chiTietId, String action) {
        GioHangChiTiet chiTiet = gioHangChiTietRepository.findById(chiTietId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm trong giỏ hàng"));

        if ("increase".equals(action)) {
            chiTiet.setSoLuong(chiTiet.getSoLuong() + 1);
        } else if ("decrease".equals(action) && chiTiet.getSoLuong() > 1) {
            chiTiet.setSoLuong(chiTiet.getSoLuong() - 1);
        }

        if (chiTiet.getSoLuong() > 0) {
            chiTiet.setNgaySua(LocalDateTime.now());
            gioHangChiTietRepository.save(chiTiet);
        } else {
            gioHangChiTietRepository.delete(chiTiet);
        }
    }


    public void xoaSanPhamKhoiGioHang(Integer chiTietId) {
        gioHangChiTietRepository.deleteById(chiTietId);
    }

    public void xoaToanBoGioHang(Authentication auth) {
        GioHang gioHang = getGioHangHienTai(auth);
        gioHangChiTietRepository.deleteAll(gioHang.getChiTietList());
    }

    public int getSoLuongSanPhamTrongGio(GioHang gioHang, SanPhamChiTiet spct) {
        return gioHangChiTietRepository
                .findByGioHangAndSanPhamChiTiet(gioHang, spct)
                .map(GioHangChiTiet::getSoLuong)
                .orElse(0);
    }

}
