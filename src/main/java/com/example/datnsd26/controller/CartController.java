package com.example.datnsd26.controller;

import com.example.datnsd26.models.*;
import com.example.datnsd26.repository.*;
import com.example.datnsd26.services.cart.GioHangService;
import com.example.datnsd26.services.cart.HoaDonService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/shop")
public class CartController {
    private final GioHangService gioHangService;
    private final GioHangRepository gioHangRepository;
    private final GioHangChiTietRepository gioHangChiTietRepository;
    private final SanPhamChiTietRepository sanPhamChiTietRepository;
    private final HoaDonRepository hoaDonRepository;
    private final HoaDonChiTietRepository hoaDonChiTietRepository;
    private final HoaDonService hoaDonService;

    @GetMapping
    public String shopPage(Model model) {
        model.addAttribute("productDetailsList", sanPhamChiTietRepository.findAll());
        return "/shop/shop";
    }

    @GetMapping("/cart")
    public String viewCart(HttpSession session, Model model) {
        GioHang cart = gioHangService.getCart(session);
        double tongTamTinh = cart.getChiTietList().stream()
                .mapToDouble(item -> item.getSoLuong() * item.getSanPhamChiTiet().getGiaBanSauGiam())
                .sum();
        model.addAttribute("tongTamTinh", tongTamTinh);
        model.addAttribute("cart", cart.getChiTietList());
        return "shop/cart";
    }

    @PostMapping("/add-to-cart")
    public String addToCart(@RequestParam("productId") Integer productId,
                            @RequestParam("quantity") int quantity,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        Optional<SanPhamChiTiet> optionalProduct = sanPhamChiTietRepository.findById(productId);

        if (optionalProduct.isPresent()) {
            SanPhamChiTiet product = optionalProduct.get();

            if (quantity > product.getSoLuong()) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Hiện sản phẩm " + product.getTenSanPhamChiTiet() + " chỉ còn " + product.getSoLuong() +
                                " sản phẩm. Nếu bạn đặt hàng với số lượng lớn, hãy liên hệ với chúng tôi qua 0397818716 để được hỗ trợ tốt nhất.");
                return "redirect:/shop"; // Giữ người dùng ở lại trang /shop/
            }

            // Nếu số lượng hợp lệ, tiếp tục thêm vào giỏ hàng
            gioHangService.addToCart(session, productId, quantity);
        }

        return "redirect:/shop/cart";

    }

    @PostMapping("/remove-from-cart")
    public String removeFromCart(@RequestParam("productId") Integer productId, HttpSession session) {
        gioHangService.removeFromCart(session, productId);
        return "redirect:/shop/cart";
    }

    @PostMapping("/update-cart")
    public String updateCart(
            @RequestParam("productId") Integer productId,
            @RequestParam("action") String action,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        GioHang cart = gioHangService.getCart(session);
        if (cart == null) {
            return "redirect:/shop/cart";
        }

        // Lấy sản phẩm từ database
        Optional<SanPhamChiTiet> product = sanPhamChiTietRepository.findById(productId);
        if (product == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Sản phẩm không tồn tại.");
            return "redirect:/shop/cart";
        }

        for (GioHangChiTiet item : cart.getChiTietList()) {
            if (item.getSanPhamChiTiet().getId().equals(productId)) {
                int newQuantity = item.getSoLuong();

                if ("increase".equals(action)) {
                    newQuantity++;
                } else if ("decrease".equals(action) && item.getSoLuong() > 1) {
                    newQuantity--;
                }

                // Kiểm tra số lượng trong kho
                if (newQuantity > product.get().getSoLuong()) {
                    redirectAttributes.addFlashAttribute("errorMessage",
                            "Hiện sản phẩm " + product.get().getTenSanPhamChiTiet() + " chỉ còn " + product.get().getSoLuong() +
                                    " sản phẩm. Nếu bạn đặt hàng với số lượng lớn, hãy liên hệ với chúng tôi qua 0397818716 để được hỗ trợ tốt nhất.");
                    return "redirect:/shop/cart";
                }

                item.setSoLuong(newQuantity);
                break;
            }
        }

        return "redirect:/shop/cart";
    }

    @GetMapping("/checkout")
    public String checkoutPage(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        // Lấy giỏ hàng từ session
        GioHang cart = gioHangService.getCart(session);

        // Nếu giỏ hàng rỗng, chuyển về trang giỏ hàng
        if (cart == null || cart.getChiTietList().isEmpty()) {
            return "redirect:/shop/cart";
        }

        // Danh sách cảnh báo nếu có sản phẩm vượt quá số lượng trong kho
        List<String> warnings = new ArrayList<>();

        for (GioHangChiTiet item : cart.getChiTietList()) {
            SanPhamChiTiet product = sanPhamChiTietRepository.findById(item.getSanPhamChiTiet().getId()).orElse(null);

            if (product != null && item.getSoLuong() > product.getSoLuong()) {
                warnings.add("Sản phẩm " + product.getTenSanPhamChiTiet() + " chỉ còn " + product.getSoLuong() + " sản phẩm.");
            }
        }

        // Nếu có lỗi, quay lại giỏ hàng và hiển thị thông báo
        if (!warnings.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessages", warnings);
            return "redirect:/shop/cart";
        }

        // Nếu không có lỗi, tiếp tục tính tổng tiền và chuyển sang trang thanh toán
        double tongTamTinh = cart.getChiTietList().stream()
                .mapToDouble(item -> item.getSoLuong() * item.getSanPhamChiTiet().getGiaBanSauGiam())
                .sum();

        // Đưa dữ liệu vào model
        model.addAttribute("cart", cart.getChiTietList());
        model.addAttribute("tongTamTinh", tongTamTinh);
        model.addAttribute("hoaDon", new HoaDon()); // Dùng để binding form thanh toán

        return "shop/checkout"; // Trả về trang checkout.html
    }



    @PostMapping("/place-order")
    public String placeOrder(@ModelAttribute HoaDon hoaDon, HttpSession session) {
        // Lấy giỏ hàng từ session
        GioHang cart = gioHangService.getCart(session);
        if (cart == null || cart.getChiTietList().isEmpty()) {
            return "redirect:/shop/cart"; // Nếu giỏ hàng rỗng, quay về trang giỏ hàng
        }

        // Tính tổng tiền
        double tongTien = cart.getChiTietList().stream()
                .mapToDouble(item -> item.getSoLuong() * item.getSanPhamChiTiet().getGiaBanSauGiam())
                .sum();

        // Thiết lập thông tin đơn hàng
        hoaDon.setNgayTao(new Date());
        hoaDon.setNgayCapNhat(new Date());
        hoaDon.setHinhThucMuaHang("Có giao hàng");
        hoaDon.setPhuongThucThanhToan("Tiền mặt");
        hoaDon.setPhiVanChuyen(0f);
        hoaDon.setTongTien((float) tongTien);
        hoaDon.setTrangThai("Chờ xác nhận");
        hoaDon.setKhachHang1(null);
        hoaDon.setNhanVien(null);

        // Lưu hóa đơn vào database
//        hoaDonRepository.save(hoaDon);
        hoaDon = hoaDonService.saveHoaDon(hoaDon);

        // Lưu chi tiết đơn hàng
        for (GioHangChiTiet item : cart.getChiTietList()) {
            HoaDonChiTiet chiTiet = new HoaDonChiTiet();
            chiTiet.setHoaDon(hoaDon);
            chiTiet.setSanPhamChiTiet(item.getSanPhamChiTiet());
            chiTiet.setSoLuong(item.getSoLuong());
            chiTiet.setGiaTienSauGiam(item.getSanPhamChiTiet().getGiaBanSauGiam());

            hoaDonChiTietRepository.save(chiTiet);
        }

        // Xóa giỏ hàng sau khi đặt hàng thành công
        gioHangService.clearCart(session);

        return "redirect:/shop/order-success";
    }

    @GetMapping("/order-success")
    public String orderSuccess() {
        return "shop/order-success";
    }

}
