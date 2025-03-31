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
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

@Controller
@RequiredArgsConstructor
public class CartController {
    private final GioHangService gioHangService;
    private final GioHangRepository gioHangRepository;
    private final GioHangChiTietRepository gioHangChiTietRepository;
    private final SanPhamChiTietRepository sanPhamChiTietRepository;
    private final HoaDonRepository hoaDonRepository;
    private final HoaDonChiTietRepository hoaDonChiTietRepository;
    private final HoaDonService hoaDonService;
    private final BinhMailService mailService;
    private final LichSuHoaDonRepository lichSuHoaDonRepository;
    private final KichCoRepository kichCoRepository;


    @GetMapping("/shop")
    public String shopPage(Model model) {
        model.addAttribute("productDetailsList", sanPhamChiTietRepository.findAll());
        return "/shop/shop";
    }

    @GetMapping("/cart")
    public String viewCart(Model model, Authentication auth) {
        GioHang gioHang = gioHangService.getGioHangHienTai(auth);
        System.out.println("Gio hang ID: " + gioHang.getId());
        System.out.println("So luong san pham trong gio hang: " + gioHang.getChiTietList().size());
        float tongTamTinh = gioHang.getChiTietList().stream()
                .mapToInt(item -> item.getSoLuong() * item.getSanPhamChiTiet().getGiaBanSauGiam().intValue())
                .sum();
        model.addAttribute("gioHang", gioHang.getChiTietList());
        model.addAttribute("tongTamTinh", tongTamTinh);
        return "/shop/cart"; // Trả về trang Thymeleaf để hiển thị giỏ hàng
    }

    @PostMapping("/cart/add")
    public String addToCart(@RequestParam("productId") Integer sanPhamId,
                            @RequestParam("quantity") int soLuong,
                            Authentication auth) {
        gioHangService.themSanPhamVaoGioHang(sanPhamId, soLuong, auth);
        return "redirect:/cart";
    }

    @PostMapping("/cart/update")
    public String updateCart(@RequestParam("chiTietId") Integer chiTietId,
                             @RequestParam("action") String action) {
        gioHangService.capNhatSoLuongSanPham(chiTietId, action);
        return "redirect:/cart";
    }

    @PostMapping("/cart/remove")
    public String removeFromCart(@RequestParam("chiTietId") Integer chiTietId) {
        gioHangService.xoaSanPhamKhoiGioHang(chiTietId);
        return "redirect:/cart";
    }

    @PostMapping("/cart/clear")
    public String clearCart(Authentication auth) {
        gioHangService.xoaToanBoGioHang(auth);
        return "redirect:/cart";
    }

    @GetMapping("/checkout")
    public String checkoutPage(Model model, Authentication auth, RedirectAttributes redirectAttributes) {
        // Lấy giỏ hàng hiện tại của user
        GioHang gioHang = gioHangService.getGioHangHienTai(auth);

        // Nếu giỏ hàng rỗng, chuyển về trang giỏ hàng và hiển thị thông báo
        if (gioHang == null || gioHang.getChiTietList().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Giỏ hàng của bạn đang trống.");
            return "redirect:/shop/cart";
        }

        // Kiểm tra số lượng sản phẩm trong kho
        List<String> warnings = new ArrayList<>();
        for (GioHangChiTiet item : gioHang.getChiTietList()) {
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

        // Tính tổng tiền
        float tongTamTinh = gioHang.getChiTietList().stream()
                .mapToInt(item -> item.getSoLuong() * item.getSanPhamChiTiet().getGiaBanSauGiam().intValue())
                .sum();

        // Đưa dữ liệu vào model
        model.addAttribute("cart", gioHang.getChiTietList());
        model.addAttribute("tongTamTinh", tongTamTinh);

        // Nếu chưa có đối tượng request thì tạo mới
        if (!model.containsAttribute("hoaDonBinhRequest")) {
            model.addAttribute("hoaDonBinhRequest", new HoaDonBinhRequest());
        }

        return "shop/checkout"; // Trả về trang checkout.html
    }

    @PostMapping("/place-order")
    public String placeOrder(
            @Valid @ModelAttribute HoaDonBinhRequest hoaDonBinhRequest,
            BindingResult bindingResult,
            Model model,
            Authentication auth) {

        GioHang cart = gioHangService.getGioHangHienTai(auth);
        if (cart == null || cart.getChiTietList().isEmpty()) {
            model.addAttribute("errorMessage", "Giỏ hàng của bạn đang trống.");
            model.addAttribute("cart", cart.getChiTietList());
            model.addAttribute("hoaDonBinhRequest", hoaDonBinhRequest);
            return "shop/checkout";
        }

        float tongTamTinh = (float) cart.getChiTietList().stream()
                .mapToDouble(item -> item.getSoLuong() * item.getSanPhamChiTiet().getGiaBanSauGiam())
                .sum();

        // Kiểm tra lỗi từ @Valid
        if (bindingResult.hasErrors()) {
            System.out.println("Có lỗi không? " + bindingResult.hasErrors());
            System.out.println("Danh sách lỗi: " + bindingResult.getAllErrors());
            model.addAttribute("cart", cart.getChiTietList());
            model.addAttribute("tongTamTinh", tongTamTinh);
            model.addAttribute("hoaDonBinhRequest", hoaDonBinhRequest);  // Đảm bảo có Model
            return "shop/checkout";
        }


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
                .hinhThucMuaHang("Online")
                .ngayTao(new Date())
                .ngayCapNhat(new Date())
                .trangThai("Chờ xác nhận")
                .phiVanChuyen(tongTamTinh >= 1000000 ? 0.0f : 30000.0f)
                .tongTien(tongTamTinh)
                .phuongThucThanhToan(hoaDonBinhRequest.getPhuongThucThanhToan())
                .thanhToan(false)
                .ghiChu(hoaDonBinhRequest.getGhiChu())
                .khachHang(null)
                .nhanVien(null)
                .build();

        // Lưu hóa đơn
        hoaDonService.saveHoaDon(hoaDon);

        lichSuHoaDonRepository.save(LichSuHoaDon.builder().trangThai("Đặt hàng").hoaDon(hoaDon).build());
        lichSuHoaDonRepository.save(LichSuHoaDon.builder().trangThai("Chờ xác nhận").hoaDon(hoaDon).build());

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
//        gioHangService.clearCart(session);

        // Gửi email xác nhận
        try {
            System.out.println("Đang gửi email xác nhận đến: " + hoaDon.getEmail());
            mailService.sendOrderConfirmation(hoaDon.getEmail(), "Chi tiết đơn hàng: " + hoaDon.getMaHoaDon());
            System.out.println("Email đã được gửi.");
        } catch (MessagingException e) {
            e.printStackTrace();
        }

        return "redirect:/order-success";
    }



    @GetMapping("/order-success")
    public String orderSuccess() {
        return "shop/order-success";
    }


}
