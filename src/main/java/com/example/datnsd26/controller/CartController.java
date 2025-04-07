package com.example.datnsd26.controller;

import com.example.datnsd26.Dto.HoaDonBinhRequest;
import com.example.datnsd26.models.*;
import com.example.datnsd26.repository.*;
import com.example.datnsd26.services.BinhMailService;
import com.example.datnsd26.services.cart.GioHangService;
import com.example.datnsd26.services.cart.HoaDonService;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
    public String addToCart(@RequestParam("productId") Integer sanPhamChiTietId,
                            @RequestParam("quantity") int soLuongThem,
                            Authentication auth,
                            RedirectAttributes redirect) {
        try {
            // Lấy thông tin sản phẩm chi tiết
            SanPhamChiTiet spct = sanPhamChiTietRepository.findById(sanPhamChiTietId).orElse(null);
            if (spct == null) {
                redirect.addFlashAttribute("errorMessage", "Sản phẩm không tồn tại.");
                return "redirect:/shop";
            }
            // Kiểm tra trạng thái sản phẩm trước khi thêm vào giỏ hàng
            if (!spct.getTrangThai()) {
                redirect.addFlashAttribute("errorMessage", "Sản phẩm này đã ngừng kinh doanh.");
                return "redirect:/shop/product/details/" + spct.getId(); // Trả về trang chi tiết
            }
            // Lấy giỏ hàng hiện tại của người dùng
            GioHang gioHang = gioHangService.getGioHangHienTai(auth);

            // Tính số lượng đã có sẵn trong giỏ hàng
            int daCoTrongGio = gioHangService.getSoLuongSanPhamTrongGio(gioHang, spct);// Giả sử đã có hàm này
            int soLuongTon = spct.getSoLuong();

            // Nếu tổng vượt quá số lượng tồn
            if (soLuongThem + daCoTrongGio > soLuongTon) {
                redirect.addFlashAttribute("errorMessage",
                        "Bạn đã thêm tối đa số lượng còn lại của sản phẩm này vào giỏ. " +
                                "Liên hệ 1900 6680 để được hỗ trợ đặt hàng với số lượng lớn hơn.");
                return "redirect:/shop/product/details/" + spct.getId();
            }

            // Nếu hợp lệ thì thêm vào giỏ hàng
            gioHangService.themSanPhamVaoGioHang(sanPhamChiTietId, soLuongThem, auth);
            return "redirect:/cart";

        } catch (RuntimeException ex) {
            redirect.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/shop/product/details/" + sanPhamChiTietId;
        }
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
            return "redirect:/cart";
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
            return "redirect:/cart";
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
            Authentication auth,
            RedirectAttributes redirectAttributes) {

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
            model.addAttribute("cart", cart.getChiTietList());
            model.addAttribute("tongTamTinh", tongTamTinh);
            model.addAttribute("hoaDonBinhRequest", hoaDonBinhRequest);
            return "shop/checkout";
        }

        // Tạo đối tượng HoaDon
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
                .phuongThucThanhToan("Thanh toán khi nhận hàng")
                .thanhToan(true)
                .ghiChu(hoaDonBinhRequest.getGhiChu())
                .khachHang(null)
                .nhanVien(null)
                .build();

        hoaDonService.saveHoaDon(hoaDon);
        lichSuHoaDonRepository.save(LichSuHoaDon.builder().trangThai("Đặt hàng").hoaDon(hoaDon).build());
        lichSuHoaDonRepository.save(LichSuHoaDon.builder().trangThai("Chờ xác nhận").hoaDon(hoaDon).build());

        // Lưu chi tiết hóa đơn và trừ số lượng sản phẩm
        List<HoaDonChiTiet> chiTietList = new ArrayList<>();
        for (GioHangChiTiet item : cart.getChiTietList()) {
            SanPhamChiTiet sanPham = sanPhamChiTietRepository.findById(item.getSanPhamChiTiet().getId()).orElseThrow();

            if (sanPham.getSoLuong() < item.getSoLuong()) {
                model.addAttribute("errorMessage", "Sản phẩm " + sanPham.getSanPham().getTenSanPham() + " không đủ hàng.");
                return "shop/checkout";
            }

            sanPham.setSoLuong(sanPham.getSoLuong() - item.getSoLuong());
            sanPhamChiTietRepository.save(sanPham);

            HoaDonChiTiet chiTiet = new HoaDonChiTiet();
            chiTiet.setHoaDon(hoaDon);
            chiTiet.setSanPhamChiTiet(item.getSanPhamChiTiet());
            chiTiet.setSoLuong(item.getSoLuong());
            chiTiet.setGiaTienSauGiam(item.getSanPhamChiTiet().getGiaBanSauGiam());
            hoaDonChiTietRepository.save(chiTiet);

            chiTietList.add(chiTiet);
        }

        // Gán danh sách sản phẩm vào hóa đơn và cập nhật lại trong DB
        hoaDon.setDanhSachSanPham(chiTietList);
        hoaDonService.saveHoaDon(hoaDon);

        // Gửi email xác nhận đơn hàng
        try {
            mailService.sendOrderConfirmation(hoaDon);
        } catch (MessagingException e) {
            e.printStackTrace();
        }

        // Xóa giỏ hàng
        gioHangRepository.delete(cart);

        model.addAttribute("successMessage", "Đặt hàng thành công. Vui lòng kiểm tra email. Chúng tôi sẽ liên hệ với bạn qua số điện thoại để xác nhận đơn hàng.");
        model.addAttribute("cart", new ArrayList<>());
        model.addAttribute("tongTamTinh", 0);
        model.addAttribute("hoaDonBinhRequest", new HoaDonBinhRequest());
        return "shop/checkout";
    }

    @GetMapping("/order-success")
    public String orderSuccess() {
        return "shop/order-success";
    }


}
