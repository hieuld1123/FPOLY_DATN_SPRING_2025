package com.example.datnsd26.controller;

import com.example.datnsd26.models.GioHangChiTiet;
import com.example.datnsd26.services.cart.GioHangService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartApiController {

    private final GioHangService gioHangService;

    public CartApiController(GioHangService gioHangService) {
        this.gioHangService = gioHangService;
    }

    @PostMapping("/update")
    public ResponseEntity<?> updateQuantity(@RequestParam("chiTietId") Integer chiTietId,
                                            @RequestParam("action") String action,
                                            Authentication auth) {
        try {
            GioHangChiTiet updatedItem = gioHangService.capNhatSoLuongSanPham(chiTietId, action); // ✅ Trả về đối tượng đã cập nhật

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("newQuantity", updatedItem.getSoLuong()); // ✅ Gửi về số lượng mới

            // (Tùy chọn) Gửi thêm tổng tiền sản phẩm hoặc tổng giỏ hàng
            response.put("totalPriceFormatted", updatedItem.getTongTienFormatted()); // ✅ Thêm dòng này
            response.put("cartTotalFormatted", gioHangService.getTongTienGioHangFormatted(auth));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/remove")
    @ResponseBody
    public Map<String, Object> xoaSanPhamKhoiGio(
            @RequestParam("chiTietId") Integer chiTietId,
            Authentication auth
    ) {
        Map<String, Object> response = new HashMap<>();

        try {
            gioHangService.xoaSanPhamKhoiGioHang(chiTietId);
            response.put("success", true);
            response.put("cartTotalFormatted", gioHangService.getTongTienGioHangFormatted(auth));
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Không thể xóa sản phẩm");
        }

        return response;
    }


}

