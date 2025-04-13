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
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/shop/")
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
    private final KhachHangRepository khachHangRepository;


    @GetMapping("/shop")
    public String shopPage(Model model) {
        model.addAttribute("productDetailsList", sanPhamChiTietRepository.findAll());
        return "/shop/shop";
    }

    @GetMapping("/cart")
    public String viewCart(Model model, Authentication auth) {
        GioHang gioHang = gioHangService.getGioHangHienTai(auth);

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
            return "redirect:/shop/cart";

        } catch (RuntimeException ex) {
            redirect.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/shop/product/details/" + sanPhamChiTietId;
        }
    }

    @PostMapping("/cart/remove")
    public String removeFromCart(@RequestParam("chiTietId") Integer chiTietId) {
        gioHangService.xoaSanPhamKhoiGioHang(chiTietId);
        return "redirect:/shop/cart";
    }

    @PostMapping("/cart/clear")
    public String clearCart(Authentication auth) {
        gioHangService.xoaToanBoGioHang(auth);
        return "redirect:/shop/cart";
    }

    @PostMapping("/checkout")
    public String goToCheckout(@RequestParam(value = "selectedIds", required = false) List<Integer> selectedIds,
                               Model model,
                               Authentication auth,
                               RedirectAttributes redirectAttributes,
                               HttpSession session) {
        List<GioHangChiTiet> gioHangHienTai = gioHangService.getGioHangHienTai(auth).getChiTietList();
        if (gioHangHienTai == null || gioHangHienTai.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Giỏ hàng của bạn hiện đang trống.");
            return "redirect:/shop/cart";
        }

        if (selectedIds == null || selectedIds.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng chọn ít nhất 1 sản phẩm để thanh toán.");
            return "redirect:/shop/cart"; // quay lại trang giỏ hàng
        }

        if (!model.containsAttribute("hoaDonBinhRequest")) {
            model.addAttribute("hoaDonBinhRequest", new HoaDonBinhRequest());
        }

        List<GioHangChiTiet> danhSachThanhToan = gioHangChiTietRepository.findAllById(selectedIds);
        List<String> danhSachLoi = new ArrayList<>();
        List<GioHangChiTiet> danhSachHopLe = new ArrayList<>();

        for (GioHangChiTiet gioHangChiTiet : danhSachThanhToan) {
            SanPhamChiTiet spct = gioHangChiTiet.getSanPhamChiTiet();

            String tenSanPhamFull = spct.getSanPham().getTenSanPham()
                    + " - màu " + spct.getMauSac().getTenMauSac()
                    + " - size " + spct.getKichCo().getTen();

            if (!spct.getTrangThai()) {
                danhSachLoi.add("Sản phẩm \"" + tenSanPhamFull + "\" hiện đã ngưng kinh doanh.");
                continue;
            }

            if (gioHangChiTiet.getSoLuong() > spct.getSoLuong()) {
                danhSachLoi.add("Sản phẩm \"" + tenSanPhamFull + "\" chỉ còn " + spct.getSoLuong() + " sản phẩm trong hệ thống.");
                continue;
            }

            // Nếu hợp lệ thì thêm vào danh sách hiển thị
            danhSachHopLe.add(gioHangChiTiet);
        }

        if (!danhSachLoi.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Một số sản phẩm không hợp lệ:");
            redirectAttributes.addFlashAttribute("errors", danhSachLoi);
            return "redirect:/shop/cart";
        }

        float tongTamTinh = danhSachHopLe.stream()
                .mapToInt(item -> item.getSoLuong() * item.getSanPhamChiTiet().getGiaBanSauGiam().intValue())
                .sum();

        // 4. Xử lý Khách Hàng
        KhachHang khachHang = null;
        if (auth != null && auth.isAuthenticated()) {
            TaiKhoan taiKhoan = (TaiKhoan) auth.getPrincipal();
            khachHang = khachHangRepository.findByTaiKhoan(taiKhoan);
        }

        // Sau khi xử lý danhSachHopLe thành công trong controller /checkout:
        session.setAttribute("selectedIds", selectedIds);
        model.addAttribute("tongTamTinh", tongTamTinh);
        model.addAttribute("cart", danhSachHopLe);
        return "shop/checkout";
    }


    @PostMapping("place-order")
    public String placeOrder(
            @Valid @ModelAttribute HoaDonBinhRequest hoaDonBinhRequest,
            BindingResult bindingResult,
            Model model,
            Authentication auth,
            RedirectAttributes redirectAttributes,
            HttpSession session) {

        GioHang cart = gioHangService.getGioHangHienTai(auth);
        if (cart == null || cart.getChiTietList().isEmpty()) {
            model.addAttribute("errorMessage", "Giỏ hàng của bạn đang trống.");
            model.addAttribute("cart", cart != null ? cart.getChiTietList() : new ArrayList<>());
            model.addAttribute("hoaDonBinhRequest", hoaDonBinhRequest);
            return "shop/checkout";
        }

        // Lấy danh sách ID sản phẩm được chọn từ session
        List<Integer> selectedIds = (List<Integer>) session.getAttribute("selectedIds");
        if (selectedIds == null || selectedIds.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng chọn sản phẩm để thanh toán.");
            return "redirect:/shop/cart";
        }

        List<GioHangChiTiet> danhSachThanhToan = gioHangChiTietRepository.findAllById(selectedIds);
        List<String> danhSachLoi = new ArrayList<>();

        for (GioHangChiTiet gioHangChiTiet : danhSachThanhToan) {
            SanPhamChiTiet spct = gioHangChiTiet.getSanPhamChiTiet();
            String tenSanPhamFull = spct.getSanPham().getTenSanPham()
                    + " - màu " + spct.getMauSac().getTenMauSac()
                    + " - size " + spct.getKichCo().getTen();

            if (!spct.getTrangThai()) {
                danhSachLoi.add("Sản phẩm \"" + tenSanPhamFull + "\" hiện đã ngưng kinh doanh.");
            } else if (gioHangChiTiet.getSoLuong() > spct.getSoLuong()) {
                danhSachLoi.add("Sản phẩm \"" + tenSanPhamFull + "\" chỉ còn " + spct.getSoLuong() + " sản phẩm trong hệ thống.");
            }
        }

        if (!danhSachLoi.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Một số sản phẩm không hợp lệ:");
            redirectAttributes.addFlashAttribute("errors", danhSachLoi);
            return "redirect:/shop/cart";
        }

        float tongTamTinh = (float) danhSachThanhToan.stream()
                .mapToDouble(item -> item.getSoLuong() * item.getSanPhamChiTiet().getGiaBanSauGiam())
                .sum();

        // Validate thông tin giao hàng
        if (bindingResult.hasErrors()) {
            model.addAttribute("cart", cart.getChiTietList());  // hiển thị tất cả sản phẩm, không lọc
            model.addAttribute("tongTamTinh", tongTamTinh);
            model.addAttribute("hoaDonBinhRequest", hoaDonBinhRequest);
            return "shop/checkout";
        }

        // Tạo hóa đơn
        HoaDon hoaDon = HoaDon.builder()
                .tenNguoiNhan(hoaDonBinhRequest.getTenNguoiNhan())
                .sdtNguoiNhan(hoaDonBinhRequest.getSdtNguoiNhan())
                .email(hoaDonBinhRequest.getEmail())
                .tinh(hoaDonBinhRequest.getTinh())
                .quan(hoaDonBinhRequest.getQuan())
                .xa(hoaDonBinhRequest.getXa())
                .diaChiNguoiNhan(hoaDonBinhRequest.getDiaChiNguoiNhan())
                .hinhThucMuaHang("Online")
                .ngayTao(new Date())
                .ngayCapNhat(new Date())
                .trangThai("Chờ xác nhận")
                .phiVanChuyen(tongTamTinh > 1000000 ? 0.0f : 30000.0f)
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

        // Lưu chi tiết hóa đơn và cập nhật tồn kho
        List<HoaDonChiTiet> chiTietList = new ArrayList<>();
        for (GioHangChiTiet item : danhSachThanhToan) {
            SanPhamChiTiet sanPham = sanPhamChiTietRepository.findById(item.getSanPhamChiTiet().getId()).orElseThrow();

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

        hoaDon.setDanhSachSanPham(chiTietList);
        hoaDonService.saveHoaDon(hoaDon);

        // Gửi email
        try {
            mailService.sendOrderConfirmation(hoaDon);
        } catch (MessagingException e) {
            e.printStackTrace();
        }

        // Xóa giỏ hàng
//        gioHangRepository.delete(cart);

        // Xóa session selectedIds sau khi đặt hàng xong
        session.removeAttribute("selectedIds");

        model.addAttribute("successMessage", "Đặt hàng thành công. Vui lòng kiểm tra email. Chúng tôi sẽ liên hệ với bạn qua số điện thoại để xác nhận đơn hàng.");
        model.addAttribute("cart", new ArrayList<>());
        model.addAttribute("tongTamTinh", 0);
        model.addAttribute("hoaDonBinhRequest", new HoaDonBinhRequest());
        return "shop/checkout";
    }

}
