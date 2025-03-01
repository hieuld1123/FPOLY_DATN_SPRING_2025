package com.example.datnsd26.services.cart;

import com.example.datnsd26.models.GioHang;
import com.example.datnsd26.models.GioHangChiTiet;
import com.example.datnsd26.models.SanPhamChiTiet;
import com.example.datnsd26.repository.SanPhamChiTietRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GioHangService {
    private final SanPhamChiTietRepository sanPhamChiTietRepository;

    public void addToCart(HttpSession session, Integer productId, int quantity) {
        GioHang gioHang = (GioHang) session.getAttribute("cart");
        if (gioHang == null) {
            gioHang = new GioHang();
            gioHang.setChiTietList(new java.util.ArrayList<>());
            session.setAttribute("cart", gioHang);
        }

        Optional<SanPhamChiTiet> optionalProduct = sanPhamChiTietRepository.findById(productId);
        GioHang finalGioHang = gioHang;
        optionalProduct.ifPresent(product -> {
            // Kiểm tra xem sản phẩm đã có trong giỏ chưa
            Optional<GioHangChiTiet> existingDetail = finalGioHang.getChiTietList().stream()
                    .filter(detail -> detail.getSanPhamChiTiet().getId().equals(productId))
                    .findFirst();

            if (existingDetail.isPresent()) {
                existingDetail.get().setSoLuong(existingDetail.get().getSoLuong() + quantity);
            } else {
                GioHangChiTiet newDetail = new GioHangChiTiet();
                newDetail.setSanPhamChiTiet(product);
                newDetail.setSoLuong(quantity);
                newDetail.setNgayTao(LocalDateTime.now());
                finalGioHang.getChiTietList().add(newDetail);
            }
        });
    }

    public GioHang getCart(HttpSession session) {
        GioHang gioHang = (GioHang) session.getAttribute("cart");
        return gioHang != null ? gioHang : new GioHang();
    }

    public void clearCart(HttpSession session) {
        session.removeAttribute("cart");
    }

    public void removeFromCart(HttpSession session, Integer productId) {
        GioHang gioHang = (GioHang) session.getAttribute("cart");
        if (gioHang != null) {
            gioHang.getChiTietList().removeIf(detail -> detail.getSanPhamChiTiet().getId().equals(productId));
            session.setAttribute("cart", gioHang); // Cập nhật lại session
        }
    }
}
