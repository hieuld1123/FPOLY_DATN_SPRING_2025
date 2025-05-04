package com.example.datnsd26.controller;

import com.example.datnsd26.models.GioHangChiTiet;
import com.example.datnsd26.repository.GioHangChiTietRepository;
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
    private static final long LIMIT_GIO_HANG = 100000000L; // 100 triệu
    private final GioHangChiTietRepository gioHangChiTietRepository;

    public CartApiController(GioHangService gioHangService, GioHangChiTietRepository gioHangChiTietRepository) {
        this.gioHangService = gioHangService;
        this.gioHangChiTietRepository = gioHangChiTietRepository;
    }

//    @PostMapping("/update")
//    public ResponseEntity<?> updateQuantity(@RequestParam("chiTietId") Integer chiTietId,
//                                            @RequestParam("action") String action,
//                                            Authentication auth) {
//        try {
//            GioHangChiTiet updatedItem = gioHangService.capNhatSoLuongSanPham(chiTietId, action); // ✅ Trả về đối tượng đã cập nhật
//
//            Map<String, Object> response = new HashMap<>();
//            response.put("success", true);
//            response.put("newQuantity", updatedItem.getSoLuong()); // ✅ Gửi về số lượng mới
//
//            // (Tùy chọn) Gửi thêm tổng tiền sản phẩm hoặc tổng giỏ hàng
//            response.put("totalPriceFormatted", updatedItem.getTongTienFormatted()); // ✅ Thêm dòng này
//            response.put("cartTotalFormatted", gioHangService.getTongTienGioHangFormatted(auth));
//            return ResponseEntity.ok(response);
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
//                    "success", false,
//                    "message", e.getMessage()
//            ));
//        }
//    }
@PostMapping("/update")
public ResponseEntity<?> updateQuantity(@RequestParam("chiTietId") Integer chiTietId,
                                        @RequestParam("action") String action,
                                        Authentication auth) {
    try {
        GioHangChiTiet item = gioHangChiTietRepository.findById(chiTietId).orElse(null);
        if (item == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Không tìm thấy sản phẩm trong giỏ hàng"
            ));
        }

        int currentQuantity = item.getSoLuong();
        int newQuantity = currentQuantity;

        if ("increase".equals(action)) {
            newQuantity++;
        } else if ("decrease".equals(action) && currentQuantity > 1) {
            newQuantity--;
        }

        if (newQuantity == currentQuantity) {
            // Không có sự thay đổi số lượng
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("newQuantity", currentQuantity);
            response.put("totalPriceFormatted", gioHangService.formatCurrency(currentQuantity * item.getSanPhamChiTiet().getGiaBanSauGiam()));
            response.put("cartTotalFormatted", gioHangService.getTongTienGioHangFormatted(auth));
            return ResponseEntity.ok(response);
        }

        // Kiểm tra vượt quá giới hạn giỏ hàng
        float giaMoiSanPham = item.getSanPhamChiTiet().getGiaBanSauGiam();
        float giaTriSanPhamThayDoi = giaMoiSanPham * (newQuantity - currentQuantity);
        float tongGiaTriGioHangHienTai = gioHangService.tinhTongGiaTriGioHangFloat(gioHangService.getGioHangHienTai(auth));
        long tongGiaTriDuKien = (long) (tongGiaTriGioHangHienTai + giaTriSanPhamThayDoi);

        if (tongGiaTriDuKien > LIMIT_GIO_HANG) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Tổng giá trị giỏ hàng không được vượt quá 100 triệu đồng."
            ));
        }

        GioHangChiTiet updatedItem = gioHangService.capNhatSoLuongSanPham(chiTietId, action);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("newQuantity", updatedItem.getSoLuong());
        response.put("totalPriceFormatted", gioHangService.formatCurrency(updatedItem.getSoLuong() * item.getSanPhamChiTiet().getGiaBanSauGiam()));
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

