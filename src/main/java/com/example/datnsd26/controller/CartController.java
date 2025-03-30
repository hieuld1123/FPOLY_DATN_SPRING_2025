package com.example.datnsd26.controller;

import com.example.datnsd26.Dto.HoaDonBinhRequest;
import com.example.datnsd26.models.*;
import com.example.datnsd26.repository.*;
import com.example.datnsd26.services.BinhMailService;
import com.example.datnsd26.services.cart.GioHangService;
import com.example.datnsd26.services.cart.HoaDonService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

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
    private final BinhMailService mailService;


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
                        "Hiện sản phẩm " + product.getSanPham().getTenSanPham() + " chỉ còn " + product.getSoLuong() +
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
                            "Hiện sản phẩm " + product.get().getSanPham().getTenSanPham() + " chỉ còn " + product.get().getSoLuong() +
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
                warnings.add("Sản phẩm " + product.getSanPham().getTenSanPham() + " chỉ còn " + product.getSoLuong() + " sản phẩm.");
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
        if (!model.containsAttribute("hoaDonBinhRequest")) {
            model.addAttribute("hoaDonBinhRequest", new HoaDonBinhRequest());
        } // Dùng để binding form thanh toán
        return "shop/checkout"; // Trả về trang checkout.html
    }


    @PostMapping("/place-order")
    public String placeOrder(
            @Valid @ModelAttribute HoaDonBinhRequest hoaDonBinhRequest,
            BindingResult bindingResult,
            Model model,
            HttpSession session) {

        GioHang cart = gioHangService.getCart(session);
        if (cart == null || cart.getChiTietList().isEmpty()) {
            model.addAttribute("errorMessage", "Giỏ hàng của bạn đang trống.");
            model.addAttribute("cart", cart.getChiTietList());
            model.addAttribute("hoaDonBinhRequest", hoaDonBinhRequest);
            return "shop/checkout";
        }

        // Kiểm tra lỗi từ @Valid
        if (bindingResult.hasErrors()) {
            System.out.println("Có lỗi không? " + bindingResult.hasErrors());
            System.out.println("Danh sách lỗi: " + bindingResult.getAllErrors());
            model.addAttribute("cart", cart.getChiTietList());
            model.addAttribute("hoaDonBinhRequest", hoaDonBinhRequest);  // Đảm bảo có Model
            return "shop/checkout";
        }

        float tongTamTinh = (float) cart.getChiTietList().stream()
                .mapToDouble(item -> item.getSoLuong() * item.getSanPhamChiTiet().getGiaBanSauGiam())
                .sum();

        // Tạo đối tượng HoaDon từ HoaDonRequest
        HoaDon hoaDon = HoaDon.builder()
                .tenNguoiNhan(hoaDonBinhRequest.getTenNguoiNhan())
                .sdtNguoiNhan(hoaDonBinhRequest.getSdtNguoiNhan())
                .email(hoaDonBinhRequest.getEmail())
                .tinh(hoaDonBinhRequest.getTinh())
                .quan(hoaDonBinhRequest.getQuan())
                .xa(hoaDonBinhRequest.getXa())
                .diaChiNguoiNhan(hoaDonBinhRequest.getTinh() + ", " +
                        hoaDonBinhRequest.getQuan() + ", " +
                        hoaDonBinhRequest.getXa() + ", " +
                        hoaDonBinhRequest.getDiaChiNguoiNhan())
                .hinhThucMuaHang("Có giao hàng")
                .ngayTao(new Date())
                .ngayCapNhat(new Date())
                .trangThai("Chờ xác nhận")
                .phiVanChuyen(tongTamTinh >= 1000000 ? 0.0f : 30000.0f)
                .tongTien(tongTamTinh)
                .phuongThucThanhToan(hoaDonBinhRequest.getPhuongThucThanhToan())
                .ghiChu(hoaDonBinhRequest.getGhiChu())
                .khachHang1(null)
                .nhanVien(null)
                .build();

        // Lưu hóa đơn
        hoaDonService.saveHoaDon(hoaDon);

        // Lưu chi tiết hóa đơn

        // Lưu chi tiết hóa đơn và trừ số lượng sản phẩm
        for (GioHangChiTiet item : cart.getChiTietList()) {
            SanPhamChiTiet sanPham = sanPhamChiTietRepository.findById(item.getSanPhamChiTiet().getId()).orElseThrow();

            // Kiểm tra số lượng còn lại
            if (sanPham.getSoLuong() < item.getSoLuong()) {
                model.addAttribute("errorMessage", "Sản phẩm " + sanPham.getSanPham().getTenSanPham() + " không đủ hàng.");
                return "shop/checkout";
            }

            // Trừ số lượng trong database
            sanPham.setSoLuong(sanPham.getSoLuong() - item.getSoLuong());
            sanPhamChiTietRepository.save(sanPham);

            HoaDonChiTiet chiTiet = new HoaDonChiTiet();
            chiTiet.setHoaDon(hoaDon);
            chiTiet.setSanPhamChiTiet(item.getSanPhamChiTiet());
            chiTiet.setSoLuong(item.getSoLuong());
            chiTiet.setGiaTienSauGiam(item.getSanPhamChiTiet().getGiaBanSauGiam());
            hoaDonChiTietRepository.save(chiTiet);
        }

        // Xóa giỏ hàng sau khi đặt hàng thành công
        gioHangService.clearCart(session);

        // Gửi email xác nhận
        try {
            System.out.println("Đang gửi email xác nhận đến: " + hoaDon.getEmail());
            mailService.sendOrderConfirmation(hoaDon.getEmail(), "Chi tiết đơn hàng: " + hoaDon.getMaHoaDon());
            System.out.println("Email đã được gửi.");
        } catch (MessagingException e) {
            e.printStackTrace();
        }

        return "redirect:/shop/order-success";
    }



    @GetMapping("/order-success")
    public String orderSuccess() {
        return "shop/order-success";
    }

    @ExceptionHandler(Exception.class)
    public String handleException(Exception e, RedirectAttributes redirectAttributes) {
        e.printStackTrace();
        redirectAttributes.addFlashAttribute("errorMessage", "Lỗi hệ thống: " + e.getMessage());
        return "redirect:/shop/checkout";
    }

}
