//package com.example.datnsd26.controller;
//
//import com.example.datnsd26.models.*;
//import com.example.datnsd26.repository.*;
//import com.example.datnsd26.services.cart.GioHangService;
//import com.example.datnsd26.services.cart.HoaDonService;
//import jakarta.servlet.http.HttpSession;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.Date;
//
//@Controller
//@RequiredArgsConstructor
//@RequestMapping("/shop")
//public class CartController {
//    private final GioHangService gioHangService;
//    private final GioHangRepository gioHangRepository;
//    private final GioHangChiTietRepository gioHangChiTietRepository;
//    private final SanPhamChiTietRepository sanPhamChiTietRepository;
//    private final HoaDonRepository hoaDonRepository;
//    private final HoaDonChiTietRepository hoaDonChiTietRepository;
//    private final HoaDonService hoaDonService;
//
//    @GetMapping
//    public String shopPage(Model model) {
//        model.addAttribute("productDetailsList", sanPhamChiTietRepository.findAll());
//        return "/shop/shop";
//    }
//
//    @GetMapping("/cart")
//    public String viewCart(HttpSession session, Model model) {
//        GioHang cart = gioHangService.getCart(session);
//        double tongTamTinh = cart.getChiTietList().stream()
//                .mapToDouble(item -> item.getSoLuong() * item.getSanPhamChiTiet().getGiaBanSauGiam())
//                .sum();
//        model.addAttribute("tongTamTinh", tongTamTinh);
//        model.addAttribute("cart", cart.getChiTietList());
//        return "shop/cart";
//    }
//
//    @PostMapping("/add-to-cart")
//    public String addToCart(@RequestParam("productId") Integer productId,
//                            @RequestParam("quantity") int quantity,
//                            HttpSession session) {
//        gioHangService.addToCart(session, productId, quantity);
//        return "redirect:/shop/cart";
//    }
//
//    @PostMapping("/remove-from-cart")
//    public String removeFromCart(@RequestParam("productId") Integer productId, HttpSession session) {
//        gioHangService.removeFromCart(session, productId);
//        return "redirect:/shop/cart";
//    }
//
//    @GetMapping("/checkout")
//    public String checkoutPage(Model model, HttpSession session) {
//        // Lấy giỏ hàng từ session
//        GioHang cart = gioHangService.getCart(session);
//
//        // Nếu giỏ hàng rỗng, chuyển về trang giỏ hàng
//        if (cart == null || cart.getChiTietList().isEmpty()) {
//            return "redirect:/shop/cart";
//        }
//
//        // Tính tổng tiền
//        double tongTamTinh = cart.getChiTietList().stream()
//                .mapToDouble(item -> item.getSoLuong() * item.getSanPhamChiTiet().getGiaBanSauGiam())
//                .sum();
//
//        // Đưa dữ liệu vào model
//        model.addAttribute("cart", cart.getChiTietList());
//        model.addAttribute("tongTamTinh", tongTamTinh);
//        model.addAttribute("hoaDon", new HoaDon()); // Dùng để binding form
//
//        return "shop/checkout"; // Trả về trang checkout.html
//    }
//
//    @PostMapping("/place-order")
//    public String placeOrder(@ModelAttribute HoaDon hoaDon, HttpSession session) {
//        // Lấy giỏ hàng từ session
//        GioHang cart = gioHangService.getCart(session);
//        if (cart == null || cart.getChiTietList().isEmpty()) {
//            return "redirect:/shop/cart"; // Nếu giỏ hàng rỗng, quay về trang giỏ hàng
//        }
//
//        // Tính tổng tiền
//        double tongTien = cart.getChiTietList().stream()
//                .mapToDouble(item -> item.getSoLuong() * item.getSanPhamChiTiet().getGiaBanSauGiam())
//                .sum();
//
//        // Thiết lập thông tin đơn hàng
//        hoaDon.setNgayTao(new Date());
//        hoaDon.setNgayCapNhat(new Date());
//        hoaDon.setHinhThucMuaHang("Có giao hàng");
//        hoaDon.setPhuongThucThanhToan("Tiền mặt");
//        hoaDon.setPhiVanChuyen(0f);
//        hoaDon.setTongTien((float) tongTien);
//        hoaDon.setTrangThai("Chờ xác nhận");
//        hoaDon.setKhachHang1(null);
//        hoaDon.setNhanVien(null);
//
//        // Lưu hóa đơn vào database
////        hoaDonRepository.save(hoaDon);
//        hoaDon = hoaDonService.saveHoaDon(hoaDon);
//
//        // Lưu chi tiết đơn hàng
//        for (GioHangChiTiet item : cart.getChiTietList()) {
//            HoaDonChiTiet chiTiet = new HoaDonChiTiet();
//            chiTiet.setHoaDon(hoaDon);
//            chiTiet.setSanPhamChiTiet(item.getSanPhamChiTiet());
//            chiTiet.setSoLuong(item.getSoLuong());
//            chiTiet.setGiaTienSauGiam(item.getSanPhamChiTiet().getGiaBanSauGiam());
//
//            hoaDonChiTietRepository.save(chiTiet);
//        }
//
//        // Xóa giỏ hàng sau khi đặt hàng thành công
//        gioHangService.clearCart(session);
//
//        return "redirect:/shop/order-success";
//    }
//
//    @GetMapping("/order-success")
//    public String orderSuccess() {
//        return "shop/order-success";
//    }
//
//}
